// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for how {@link SenderLinkHandler} reacts to the service closing or detaching its link. Uses real proton-j
 * endpoints so that the resulting link states can be asserted.
 */
@RunWith(JMockit.class)
public class SenderLinkHandlerTest
{
    private static final String LINK_TYPE = "cloudToDeviceSender";

    @Mocked Event mockEvent;
    @Mocked LinkStateCallback mockLinkStateCallback;

    private Connection connection;
    private Session session;
    private Sender sender;
    private SenderLinkHandler senderLinkHandler;

    @Before
    public void setUp()
    {
        connection = Proton.connection();
        connection.open();
        session = connection.session();
        session.open();
        sender = session.sender("sender");
        sender.open();
        senderLinkHandler = new CloudToDeviceMessageSenderLinkHandler(sender, "correlationId", mockLinkStateCallback);

        new Expectations()
        {
            {
                mockEvent.getLink(); result = sender; minTimes = 0;
                mockEvent.getSession(); result = session; minTimes = 0;
            }
        };
    }

    @Test
    public void onLinkRemoteDetachWhileLinkIsOpenDetachesLinkAndNotifiesCallback()
    {
        // act
        senderLinkHandler.onLinkRemoteDetach(mockEvent);

        // assert
        assertTrue("Client must respond to the service's detach with its own detach", sender.detached());
        assertEquals("Responding to a detach should not close the link", EndpointState.ACTIVE, sender.getLocalState());
        new Verifications()
        {
            {
                mockLinkStateCallback.onLinkRemoteClosedUnexpectedly(mockEvent, LINK_TYPE);
                times = 1;
            }
        };
    }

    @Test
    public void onLinkRemoteDetachAfterLinkWasClosedLocallyDoesNotNotifyCallback()
    {
        // arrange
        sender.close();

        // act
        senderLinkHandler.onLinkRemoteDetach(mockEvent);

        // assert
        assertFalse(sender.detached());
        new Verifications()
        {
            {
                mockLinkStateCallback.onLinkRemoteClosedUnexpectedly((Event) any, anyString);
                times = 0;
            }
        };
    }

    @Test
    public void onLinkRemoteDetachAfterLinkWasDetachedLocallyDoesNotNotifyCallbackAgain()
    {
        // arrange
        sender.detach();

        // act
        senderLinkHandler.onLinkRemoteDetach(mockEvent);

        // assert
        new Verifications()
        {
            {
                mockLinkStateCallback.onLinkRemoteClosedUnexpectedly((Event) any, anyString);
                times = 0;
            }
        };
    }

    @Test
    public void onLinkRemoteCloseWhileLinkIsOpenClosesLinkAndNotifiesCallback()
    {
        // act
        senderLinkHandler.onLinkRemoteClose(mockEvent);

        // assert
        assertEquals(EndpointState.CLOSED, sender.getLocalState());
        new Verifications()
        {
            {
                mockLinkStateCallback.onLinkRemoteClosedUnexpectedly(mockEvent, LINK_TYPE);
                times = 1;
            }
        };
    }

    @Test
    public void onLinkRemoteCloseAfterLinkWasClosedLocallyClosesSessionWithoutNotifyingCallback()
    {
        // arrange
        sender.close();

        // act
        senderLinkHandler.onLinkRemoteClose(mockEvent);

        // assert
        assertEquals(EndpointState.CLOSED, session.getLocalState());
        new Verifications()
        {
            {
                mockLinkStateCallback.onLinkRemoteClosedUnexpectedly((Event) any, anyString);
                times = 0;
            }
        };
    }

    @Test
    public void belongsToOnlyMatchesTheConnectionThatOwnsTheLink()
    {
        assertTrue(senderLinkHandler.belongsTo(connection));
        assertFalse(senderLinkHandler.belongsTo(Proton.connection()));
    }
}
