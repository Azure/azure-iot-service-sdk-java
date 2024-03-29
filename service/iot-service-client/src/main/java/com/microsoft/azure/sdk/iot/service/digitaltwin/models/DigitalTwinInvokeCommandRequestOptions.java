// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

/**
 * General request options that are applicable, but optional, for invoke command APIs.
 */
public final class DigitalTwinInvokeCommandRequestOptions {

    /**
     * The time (in seconds) that the service waits for the device to come online.
     * The default is 0 seconds (which means the device must already be online) and the maximum is 300 seconds.
     */
    private Integer connectTimeoutInSeconds;

    /**
     * The time (in seconds) that the service waits for the method invocation to return a response.
     * The default is 30 seconds, minimum is 5 seconds, and maximum is 300 seconds.
     */
    private Integer responseTimeoutInSeconds;

    /**
     * Sets the time (in seconds) that the service waits for the device to come online.
     * @return The number of seconds before connect timeouts occur for this request.
     */
    public Integer getConnectTimeoutInSeconds()
    {
        return connectTimeoutInSeconds;
    }

    /**
     * Sets the time (in seconds) that the service waits for the device to come online.
     * The default is 0 seconds (which means the device must already be online) and the maximum is 300 seconds.
     *
     * @param connectTimeoutInSeconds The number of seconds before connect timeouts occur for this request.
     */
    public void setConnectTimeoutInSeconds(Integer connectTimeoutInSeconds)
    {
        this.connectTimeoutInSeconds = connectTimeoutInSeconds;
    }

    /**
     * Gets the time (in seconds) that the service waits for the method invocation to return a response.
     *
     * @return The number of seconds before resposne timeouts occur for this request.
     */
    public Integer getResponseTimeoutInSeconds()
    {
        return responseTimeoutInSeconds;
    }

    /**
     * Sets the time (in seconds) that the service waits for the method invocation to return a response.
     * The default is 30 seconds, minimum is 5 seconds, and maximum is 300 seconds.
     *
     * @param responseTimeoutInSeconds The number of seconds before response timeouts occur for this request.
     */
    public void setResponseTimeoutInSeconds(Integer responseTimeoutInSeconds)
    {
        this.responseTimeoutInSeconds = responseTimeoutInSeconds;
    }
}
