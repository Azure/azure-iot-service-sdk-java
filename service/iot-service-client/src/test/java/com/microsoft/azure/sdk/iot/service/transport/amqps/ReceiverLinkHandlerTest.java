// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for how {@link ReceiverLinkHandler} reacts to the service closing or detaching its link. Uses real proton-j
 * endpoints so that the resulting link states can be asserted.
 */
@RunWith(JMockit.class)
public class ReceiverLinkHandlerTest
{
    private static final String LINK_TYPE = "messageFeedbackReceiver";

    @Mocked Event mockEvent;
    @Mocked LinkStateCallback mockLinkStateCallback;

    private Session session;
    private Receiver receiver;
    private ReceiverLinkHandler receiverLinkHandler;

    @Before
    public void setUp()
    {
        Connection connection = Proton.connection();
        connection.open();
        session = connection.session();
        session.open();
        receiver = session.receiver("receiver");
        receiver.open();
        receiverLinkHandler = new MessageFeedbackReceiverLinkHandler(receiver, mockLinkStateCallback, feedbackBatch -> AcknowledgementType.COMPLETE);

        new Expectations()
        {
            {
                mockEvent.getLink(); result = receiver; minTimes = 0;
                mockEvent.getSession(); result = session; minTimes = 0;
            }
        };
    }

    @Test
    public void onLinkRemoteDetachWhileLinkIsOpenDetachesLinkAndNotifiesCallback()
    {
        // act
        receiverLinkHandler.onLinkRemoteDetach(mockEvent);

        // assert
        assertTrue("Client must respond to the service's detach with its own detach", receiver.detached());
        assertEquals("Responding to a detach should not close the link", EndpointState.ACTIVE, receiver.getLocalState());
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
        receiver.close();

        // act
        receiverLinkHandler.onLinkRemoteDetach(mockEvent);

        // assert
        assertFalse(receiver.detached());
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
        receiver.detach();

        // act
        receiverLinkHandler.onLinkRemoteDetach(mockEvent);

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
        receiverLinkHandler.onLinkRemoteClose(mockEvent);

        // assert
        assertEquals(EndpointState.CLOSED, receiver.getLocalState());
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
        receiver.close();

        // act
        receiverLinkHandler.onLinkRemoteClose(mockEvent);

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
    public void isOpenReturnsFalseUntilTheServiceOpensTheLink()
    {
        // The link is open locally, but the service has not attached it yet
        assertFalse(receiverLinkHandler.isOpen());
    }
}
