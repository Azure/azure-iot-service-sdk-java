package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.query.SqlQueryBuilder;
import com.microsoft.azure.sdk.iot.service.query.TwinQueryResponse;
import com.microsoft.azure.sdk.iot.service.registry.serializers.ExportImportDeviceParser;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DeviceDeletionSample
{
    private static final int SLEEP_INTERVAL_MILLISECONDS = 15 * 1000;

    private static boolean ranSuccessfully = true;

    /**
     * A simple sample for deleting all devices from an iothub
     */
    public static void main(String[] args) throws InterruptedException, Exception
    {
        if (args.length != 1)
        {
            System.out.format(
                    "Expected 1 argument but received: %d.\n"
                            + "1.) Iot hub connection string to hub, this sample will delete all devices registered in this hub\n",
                    args.length);
            return;
        }

        if ((args[0] == null || args[0].isEmpty()) && (args[1] == null || args[1].isEmpty()))
        {
            throw new Exception("No connection string info provided");
        }

        String iotHubConnString = args[0];
        Thread hubCleanupRunnable = null;
        if (!(iotHubConnString == null || iotHubConnString.isEmpty() || iotHubConnString.equals(" ")))
        {
            hubCleanupRunnable = new Thread(new HubCleanupRunnable(iotHubConnString));
            hubCleanupRunnable.start();
        }

        if (hubCleanupRunnable != null)
        {
            hubCleanupRunnable.join();
        }

        if (!ranSuccessfully)
        {
            System.exit(-1);
        }
    }

    public static class HubCleanupRunnable implements Runnable {

        public String iotConnString;

        public HubCleanupRunnable(String iotConnString)
        {
            this.iotConnString = iotConnString;
        }

        @Override
        public void run() {
            TwinClient twinClient = new TwinClient(iotConnString);

            System.out.println("Querying ");

            List<String> deviceIdsToRemove = new ArrayList<>();
            String query = SqlQueryBuilder.createSqlQuery("*", SqlQueryBuilder.FromType.DEVICES, null, null);

            while (true)
            {
                TwinQueryResponse twinQuery;
                try
                {
                    twinQuery = twinClient.query(query);
                }
                catch (IotHubException e)
                {
                    e.printStackTrace();
                    ranSuccessfully = false;
                    return;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    ranSuccessfully = false;
                    return;
                }

                try
                {
                    while (twinQuery.hasNext())
                    {
                        Twin twin = twinQuery.next();
                        deviceIdsToRemove.add(twin.getDeviceId());
                        if (deviceIdsToRemove.size() >= 100) {
                            System.out.println("Queried 100 devices, now attempting to delete them");
                            break;
                        }
                    }

                    if (deviceIdsToRemove.size() == 0)
                    {
                        System.out.println("No more devices to delete");
                        return;
                    }

                    try
                    {
                        removeDevices(deviceIdsToRemove, iotConnString);
                        System.out.println("Successfully deleted a batch of devices");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        System.out.println("Failed to bulk delete, moving on to next set of devices");
                    }

                    deviceIdsToRemove.clear();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    ranSuccessfully = false;
                    System.out.println("Could not collect the full list of device ids to delete, exiting iot hub cleanup thread");
                    return;
                }

                try
                {
                    // Currently, IoT Hub doesn't have a good mechanism for throttling bulk operation requests, so this
                    // delay will help to space out the requests
                    System.out.println("Sleeping for " + SLEEP_INTERVAL_MILLISECONDS + " milliseconds before next bulk deletion");
                    Thread.sleep(SLEEP_INTERVAL_MILLISECONDS);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    // This call mimics what should be a registry manager API for adding devices in bulk. Can be removed once we add support in our
    // registry manager for this
    private static final String DELETE_IMPORT_MODE = "delete";
    public static void removeDevices(Iterable<String> deviceIds, String connectionString) throws IOException, IotHubException {
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        URL url = getBulkDeviceAddUrl(iotHubConnectionString);

        List<ExportImportDeviceParser> parsers = new ArrayList<>();
        for (String deviceId : deviceIds)
        {
            ExportImportDeviceParser exportImportDevice = new ExportImportDeviceParser();
            exportImportDevice.setId(deviceId);
            exportImportDevice.setImportMode(DELETE_IMPORT_MODE);
            parsers.add(exportImportDevice);
        }

        ExportImportDevicesParser body = new ExportImportDevicesParser();
        body.setExportImportDevices(parsers);

        String sasTokenString = new IotHubServiceSasToken(iotHubConnectionString).toString();

        HttpRequest request = new HttpRequest(url, HttpMethod.POST, body.toJson().getBytes(), sasTokenString);
        request.setHeaderField("Accept", "application/json");
        request.setHeaderField("Content-Type", "application/json");
        request.setHeaderField("charset", "utf-8");

        HttpResponse response = request.send();

        IotHubExceptionManager.httpResponseVerification(response);
    }

    public static URL getBulkDeviceAddUrl(IotHubConnectionString iotHubConnectionString) throws MalformedURLException
    {
        String stringBuilder = "https://" +
                iotHubConnectionString.getHostName() +
                "/devices/?api-version=" +
                TransportUtils.IOTHUB_API_VERSION;
        return new URL(stringBuilder);
    }
}
