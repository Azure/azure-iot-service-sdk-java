// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import org.apache.qpid.proton.engine.Event;

/**
 * Interface that defines the callbacks used by AMQP links to notify their respective AMQP sessions about state changes
 */
interface LinkStateCallback
{
    /**
     * Callback that executes when the sender link has opened successfully
     */
    void onSenderLinkRemoteOpen();

    /**
     * Callback that executes when the receiver link has opened successfully
     */
    void onReceiverLinkRemoteOpen();

    /**
     * Callback that executes when the service closes or detaches a link that this client did not close first.
     * @param event the proton event for the remote close or detach.
     * @param linkInstanceType the type of the link that was closed or detached.
     */
    void onLinkRemoteClosedUnexpectedly(Event event, String linkInstanceType);
}
