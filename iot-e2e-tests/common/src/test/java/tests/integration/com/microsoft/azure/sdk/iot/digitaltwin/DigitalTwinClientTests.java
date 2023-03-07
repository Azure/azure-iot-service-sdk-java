// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.digitaltwin;

import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.BasicDigitalTwin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponseWithHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.digitaltwin.helpers.E2ETestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DigitalTwinTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.HttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.impl.DefaultHttpProxyServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

@DigitalTwinTest
@IotHubTest
@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinClientTests extends IntegrationTest
{
    private static final int TWIN_PROPAGATION_TIMEOUT_MILLIS = 60 * 1000;
    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static RegistryClient registryClient;
    private String deviceId;
    private DeviceClient deviceClient;
    private DigitalTwinClient digitalTwinClient = null;
    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientTests_";
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8769;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter()
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Digital Twin Test: protocol={0}")
    public static Collection<Object[]> data() {
        return (List) new ArrayList(Arrays.asList(new Object[][]{
                {MQTT},
                {MQTT_WS},
                {AMQPS},
                {AMQPS_WS},
        }));
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        registryClient =
            new RegistryClient(
                IOTHUB_CONNECTION_STRING,
                RegistryClientOptions.builder()
                    .httpReadTimeoutSeconds(0)
                    .build());
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, IotHubException, IotHubClientException
    {
        this.deviceClient = createDeviceClient(protocol);
        deviceClient.open(false);
        digitalTwinClient =
            new DigitalTwinClient(
                IOTHUB_CONNECTION_STRING,
                DigitalTwinClientOptions.builder()
                    .httpReadTimeoutSeconds(0)
                    .build());
    }

    @After
    public void cleanUp() {
        try {
            deviceClient.close();
            registryClient.removeDevice(deviceId);
        } catch (Exception ex) {
            log.error("An exception occurred while closing/ deleting the device {}: {}", deviceId, ex);
        }
    }

    private DeviceClient createDeviceClient(IotHubClientProtocol protocol) throws IOException, IotHubException, URISyntaxException {
        return createDeviceClient(protocol, E2ETestConstants.THERMOSTAT_MODEL_ID);
    }

    private DeviceClient createDeviceClient(IotHubClientProtocol protocol, String modelId) throws IOException, IotHubException, URISyntaxException {
        ClientOptions options = ClientOptions.builder().modelId(modelId).build();

        this.deviceId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        Device device = new Device(deviceId, AuthenticationType.SAS);
        Device registeredDevice = registryClient.addDevice(device);
        String deviceConnectionString = Tools.getDeviceConnectionString(IOTHUB_CONNECTION_STRING, registeredDevice);
        return new DeviceClient(deviceConnectionString, protocol, options);
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDigitalTwin() {
        // act
        BasicDigitalTwin response = digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDigitalTwinWithProxy() {
        // arrange
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
        ProxyOptions proxyOptions = new ProxyOptions(proxy);
        DigitalTwinClientOptions clientOptions =
            DigitalTwinClientOptions.builder()
                .proxyOptions(proxyOptions)
                .httpReadTimeoutSeconds(0)
                .build();

        digitalTwinClient = new DigitalTwinClient(IOTHUB_CONNECTION_STRING, clientOptions);

        // act
        BasicDigitalTwin response = digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    @StandardTierHubOnlyTest
    public void digitalTwinConstructorThrowsForNegativeConnectTimeout() {
        // arrange
        DigitalTwinClientOptions clientOptions =
            DigitalTwinClientOptions.builder()
                .httpConnectTimeoutSeconds(-1)
                .build();

        digitalTwinClient = new DigitalTwinClient(IOTHUB_CONNECTION_STRING, clientOptions);
    }

    @Test(expected = IllegalArgumentException.class)
    @StandardTierHubOnlyTest
    public void digitalTwinConstructorThrowsForNegativeReadTimeout() {
        // arrange
        DigitalTwinClientOptions clientOptions =
            DigitalTwinClientOptions.builder()
                .httpReadTimeoutSeconds(-1)
                .build();

        digitalTwinClient = new DigitalTwinClient(IOTHUB_CONNECTION_STRING, clientOptions);
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDigitalTwinWithAzureSasCredential() {
        if (protocol != MQTT)
        {
            // This test is for the service client, so no need to rerun it for all the different device protocols
            return;
        }

        // arrange
        digitalTwinClient = buildDigitalTwinClientWithAzureSasCredential();

        // act
        BasicDigitalTwin response = digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), E2ETestConstants.THERMOSTAT_MODEL_ID);
    }

    @Test
    @StandardTierHubOnlyTest
    public void digitalTwinClientTokenRenewalWithAzureSasCredential()
    {
        if (protocol != MQTT)
        {
            // This test is for the service client, so no need to rerun it for all the different device protocols
            return;
        }

        IotHubConnectionString iotHubConnectionStringObj =
            IotHubConnectionStringBuilder.createIotHubConnectionString(IOTHUB_CONNECTION_STRING);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());
        digitalTwinClient = new DigitalTwinClient(iotHubConnectionStringObj.getHostName(), sasCredential);

        // get a digital twin with a valid SAS token in the AzureSasCredential instance
        // don't care about the return value, just checking that the request isn't unauthorized
        digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);

        // deliberately expire the SAS token to provoke a 401 to ensure that the digital twin client is using the shared
        // access signature that is set here.
        sasCredential.update(SasTokenTools.makeSasTokenExpired(serviceSasToken.toString()));

        try
        {
            digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
            fail("Expected get digital twin call to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (RestException e)
        {
            if (e.response().code() == 401)
            {
                log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
            }
            else
            {
                throw e;
            }
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        sasCredential.update(serviceSasToken.toString());

        // get a digital twin using the renewed shared access signature
        // don't care about the return value, just checking that the request isn't unauthorized
        digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
    }

    private static DigitalTwinClient buildDigitalTwinClientWithAzureSasCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(IOTHUB_CONNECTION_STRING);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        DigitalTwinClientOptions options = DigitalTwinClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build();
        return new DigitalTwinClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
    }
}
