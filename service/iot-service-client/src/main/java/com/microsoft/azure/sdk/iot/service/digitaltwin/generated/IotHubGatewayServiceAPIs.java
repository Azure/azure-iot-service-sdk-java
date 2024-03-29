/*
  Code generated by Microsoft (R) AutoRest Code Generator.
  Changes may cause incorrect behavior and will be lost if the code is
  regenerated.
 */

package com.microsoft.azure.sdk.iot.service.digitaltwin.generated;

import com.microsoft.rest.RestClient;

/**
 * The interface for IotHubGatewayServiceAPIs class.
 */
public interface IotHubGatewayServiceAPIs {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
    */
    RestClient restClient();

    /**
     * The default base URL.
     */
    String DEFAULT_BASE_URL = "https://fully-qualified-iothubname.azure-devices.net";

    /**
     * Gets Version of the Api..
     *
     * @return the apiVersion value.
     */
    String apiVersion();

    /**
     * Sets Version of the Api..
     *
     * @param apiVersion the apiVersion value.
     * @return the service client itself
     */
    IotHubGatewayServiceAPIs withApiVersion(String apiVersion);

    /**
     * Gets the DigitalTwins object to access its operations.
     * @return the DigitalTwins object.
     */
    DigitalTwins digitalTwins();

}
