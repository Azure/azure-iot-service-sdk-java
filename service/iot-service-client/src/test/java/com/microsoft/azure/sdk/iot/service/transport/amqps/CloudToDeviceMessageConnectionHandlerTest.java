// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.SendResult;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.impl.ConnectionImpl;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for how {@link CloudToDeviceMessageConnectionHandler} manages its cloud to device sender link across
 * connections. Uses real proton-j endpoints so that the link each connection owns can be asserted.
 */
@RunWith(JMockit.class)
public class CloudToDeviceMessageConnectionHandlerTest
{
    private static final String CONNECTION_STRING = TestAmqpConnectionHandler.CONNECTION_STRING;
    private static final String SENDER_LINK_HANDLER_FIELD = "cloudToDeviceMessageSenderLinkHandler";

    @Mocked Event mockEvent;

    private final List<ErrorContext> reportedErrors = new ArrayList<>();
    private CloudToDeviceMessageConnectionHandler connectionHandler;

    @Before
    public void setUp()
    {
        connectionHandler = new CloudToDeviceMessageConnectionHandler(
            CONNECTION_STRING, IotHubServiceClientProtocol.AMQPS, reportedErrors::add, null, null, 230);
    }

    private Connection authenticateOnNewConnection(Reactor reactor)
    {
        Connection connection = Proton.connection();
        ((ConnectionImpl) connection).setReactor(reactor);
        connection.open();
        Deencapsulation.setField(connectionHandler, "connection", connection);
        connectionHandler.onAuthenticationSucceeded();
        return connection;
    }

    private CloudToDeviceMessageSenderLinkHandler getSenderLinkHandler()
    {
        return Deencapsulation.getField(connectionHandler, SENDER_LINK_HANDLER_FIELD);
    }

    @Test
    public void onAuthenticationSucceededDoesNotOpenAnotherSenderLinkAfterProactiveRenewal()
    {
        // arrange
        Connection connection = authenticateOnNewConnection(null);
        CloudToDeviceMessageSenderLinkHandler firstSenderLinkHandler = getSenderLinkHandler();

        // act
        connectionHandler.onAuthenticationSucceeded();

        // assert
        assertNotNull(firstSenderLinkHandler);
        assertTrue(firstSenderLinkHandler.belongsTo(connection));
        assertSame(firstSenderLinkHandler, getSenderLinkHandler());
    }

    @Test
    public void onAuthenticationSucceededOpensNewSenderLinkOnNewConnection()
    {
        // arrange
        authenticateOnNewConnection(null);
        CloudToDeviceMessageSenderLinkHandler staleSenderLinkHandler = getSenderLinkHandler();

        // act
        Connection newConnection = authenticateOnNewConnection(null);

        // assert
        CloudToDeviceMessageSenderLinkHandler newSenderLinkHandler = getSenderLinkHandler();
        assertNotSame("The sender link from the previous connection must not be reused", staleSenderLinkHandler, newSenderLinkHandler);
        assertTrue(newSenderLinkHandler.belongsTo(newConnection));
        assertEquals(EndpointState.ACTIVE, newSenderLinkHandler.senderLink.getLocalState());
    }

    @Test
    public void senderLinkLossFailsQueuedMessagesAndClosesConnection()
    {
        // arrange
        final Connection connection = authenticateOnNewConnection(null);
        final CloudToDeviceMessageSenderLinkHandler senderLinkHandler = getSenderLinkHandler();
        final List<SendResult> sendResults = new ArrayList<>();
        senderLinkHandler.sendAsync("device1", null, new Message("hello"), sendResults::add, null);

        new Expectations()
        {
            {
                mockEvent.getConnection(); result = connection; minTimes = 0;
                mockEvent.getSession(); result = senderLinkHandler.senderLink.getSession(); minTimes = 0;
                mockEvent.getLink(); result = senderLinkHandler.senderLink; minTimes = 0;
                mockEvent.getSender(); result = senderLinkHandler.senderLink; minTimes = 0;
                mockEvent.getReceiver(); result = null; minTimes = 0;
                mockEvent.getTransport(); result = null; minTimes = 0;
            }
        };

        // act
        connectionHandler.onLinkRemoteClosedUnexpectedly(mockEvent, "cloudToDeviceSender");

        // assert
        assertEquals("Queued messages should fail rather than wait for a link that won't come back", 1, sendResults.size());
        assertFalse(sendResults.get(0).wasSentSuccessfully());
        assertNotNull(sendResults.get(0).getException());
        assertEquals(1, reportedErrors.size());
        assertEquals(EndpointState.CLOSED, connection.getLocalState());
    }

    @Test
    public void onReactorFinalFromPreviousReactorDoesNotClearNewSenderLink(
        @Injectable final Reactor previousReactor,
        @Injectable final Reactor currentReactor)
    {
        // arrange
        authenticateOnNewConnection(previousReactor);
        authenticateOnNewConnection(currentReactor);
        CloudToDeviceMessageSenderLinkHandler currentSenderLinkHandler = getSenderLinkHandler();

        new Expectations()
        {
            {
                mockEvent.getReactor(); returns(previousReactor, currentReactor);
            }
        };

        // act
        connectionHandler.onReactorFinal(mockEvent);

        // assert
        assertSame("A previous reactor finishing late must not clear the current connection's link", currentSenderLinkHandler, getSenderLinkHandler());

        // act
        connectionHandler.onReactorFinal(mockEvent);

        // assert
        assertNull(getSenderLinkHandler());
    }
}
