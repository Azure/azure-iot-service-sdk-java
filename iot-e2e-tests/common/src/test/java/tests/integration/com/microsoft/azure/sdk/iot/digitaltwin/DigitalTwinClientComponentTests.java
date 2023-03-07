// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.digitaltwin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.UpdateOperationUtility;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.*;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.BasicDigitalTwin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.ServiceResponseWithHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.digitaltwin.helpers.E2ETestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DigitalTwinTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@DigitalTwinTest
@IotHubTest
@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinClientComponentTests extends IntegrationTest
{

    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static RegistryClient registryClient;
    private String deviceId;
    private DeviceClient deviceClient;
    private DigitalTwinClient digitalTwinClient = null;
    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientTests_";

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter()
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Digital Twin Test: protocol={0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                {MQTT},
                {MQTT_WS},
                {AMQPS},
                {AMQPS_WS},
        });
    }

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        registryClient = new RegistryClient(IOTHUB_CONNECTION_STRING);
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, IotHubException, IotHubClientException
    {
        this.deviceClient = createDeviceClient(protocol);
        deviceClient.open(false);
        digitalTwinClient = DigitalTwinClient.createFromConnectionString(IOTHUB_CONNECTION_STRING);
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
        ClientOptions options = ClientOptions.builder().modelId(E2ETestConstants.TEMPERATURE_CONTROLLER_MODEL_ID).build();

        this.deviceId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        Device device = new Device(deviceId, AuthenticationType.SAS);
        Device registeredDevice = registryClient.addDevice(device);
        String deviceConnectionString = Tools.getDeviceConnectionString(IOTHUB_CONNECTION_STRING, registeredDevice);
        return new DeviceClient(deviceConnectionString, protocol, options);
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDigitalTwin() {
        // act
        BasicDigitalTwin response = this.digitalTwinClient.getDigitalTwin(deviceId, BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders = this.digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), E2ETestConstants.TEMPERATURE_CONTROLLER_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), E2ETestConstants.TEMPERATURE_CONTROLLER_MODEL_ID);
    }

    @Test
    @StandardTierHubOnlyTest
    public void updateDigitalTwin()
    {
        // arrange
        String newProperty = "currentTemperature";
        Integer newPropertyValue = 35;
        String newComponent = "newThermostat";
        String newComponentPath = "/newThermostat";
        Map<String, Object> componentProperties = new HashMap<>();
        componentProperties.put(newProperty, newPropertyValue);

        DigitalTwinUpdateRequestOptions optionsWithoutEtag = new DigitalTwinUpdateRequestOptions();
        optionsWithoutEtag.setIfMatch("*");

        // get digital twin and Etag before update
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders = digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class);
        DigitalTwinUpdateRequestOptions optionsWithEtag = new DigitalTwinUpdateRequestOptions();
        optionsWithEtag.setIfMatch(responseWithHeaders.headers().eTag());

        // act
        // Add component at root level - conditional update with max overload
        UpdateOperationUtility updateOperationUtility = new UpdateOperationUtility().appendAddComponentOperation(newComponentPath, componentProperties);
        digitalTwinClient.updateDigitalTwinWithResponse(deviceId, updateOperationUtility.getUpdateOperations(), optionsWithEtag);
        BasicDigitalTwin digitalTwin = digitalTwinClient.getDigitalTwinWithResponse(deviceId, BasicDigitalTwin.class).body();

        // assert
        assertEquals(E2ETestConstants.TEMPERATURE_CONTROLLER_MODEL_ID, digitalTwin.getMetadata().getModelId());
        assertTrue(digitalTwin.getCustomProperties().containsKey(newComponent));
    }
}
