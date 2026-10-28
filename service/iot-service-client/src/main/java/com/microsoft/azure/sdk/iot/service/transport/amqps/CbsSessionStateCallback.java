package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.apache.qpid.proton.engine.Event;

/**
 * Interface that defines the callbacks used by the CBS session to notify the AMQP connection handler of authentication
 * status changes.
 */
interface CbsSessionStateCallback
{
    /**
     * Executes when the CBS session has successfully authenticated the connection
     */
    void onAuthenticationSucceeded();

    /**
     * Executes when the CBS session has failed to authenticate the connection
     * @param e the reason why authentication failed.
     */
    void onAuthenticationFailed(IotHubException e);

    /**
     * Executes when the service closes or detaches a link that this client did not close first.
     * @param event the proton event for the remote close or detach.
     * @param linkInstanceType the type of the link that was closed or detached.
     */
    void onLinkRemoteClosedUnexpectedly(Event event, String linkInstanceType);
}
