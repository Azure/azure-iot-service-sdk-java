// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.ConnectionImpl;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for how {@link EventReceivingConnectionHandler} manages its receiver links across connections, and for
 * how it reports whether those links are open. Kept separate from {@link EventReceivingConnectionHandlerTest} because
 * these tests use real proton-j endpoints rather than mocked ones.
 */
@RunWith(JMockit.class)
public class EventReceivingConnectionHandlerLinkLifecycleTest
{
    private static final String CONNECTION_STRING = TestAmqpConnectionHandler.CONNECTION_STRING;
    private static final String FEEDBACK_LINK_HANDLER_FIELD = "messageFeedbackReceiverLinkHandler";
    private static final String FILE_UPLOAD_LINK_HANDLER_FIELD = "fileUploadNotificationReceiverLinkHandler";

    private static final Function<FileUploadNotification, AcknowledgementType> FILE_UPLOAD_CALLBACK = notification -> AcknowledgementType.COMPLETE;
    private static final Function<FeedbackBatch, AcknowledgementType> FEEDBACK_CALLBACK = feedbackBatch -> AcknowledgementType.COMPLETE;

    @Mocked Event mockEvent;

    private static EventReceivingConnectionHandler createConnectionHandler(
        Function<FileUploadNotification, AcknowledgementType> fileUploadCallback,
        Function<FeedbackBatch, AcknowledgementType> feedbackCallback)
    {
        return new EventReceivingConnectionHandler(
            CONNECTION_STRING, IotHubServiceClientProtocol.AMQPS, fileUploadCallback, feedbackCallback, null, null, null, 230);
    }

    private static Connection authenticateOnNewConnection(EventReceivingConnectionHandler connectionHandler, Reactor reactor)
    {
        Connection connection = Proton.connection();
        ((ConnectionImpl) connection).setReactor(reactor);
        connection.open();
        Deencapsulation.setField(connectionHandler, "connection", connection);
        connectionHandler.onAuthenticationSucceeded();
        return connection;
    }

    @Test
    public void onAuthenticationSucceededDoesNotOpenMoreReceiverLinksAfterProactiveRenewal()
    {
        // arrange
        EventReceivingConnectionHandler connectionHandler = createConnectionHandler(FILE_UPLOAD_CALLBACK, FEEDBACK_CALLBACK);
        authenticateOnNewConnection(connectionHandler, null);
        Object feedbackLinkHandler = Deencapsulation.getField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD);
        Object fileUploadLinkHandler = Deencapsulation.getField(connectionHandler, FILE_UPLOAD_LINK_HANDLER_FIELD);

        // act
        connectionHandler.onAuthenticationSucceeded();

        // assert
        assertNotNull(feedbackLinkHandler);
        assertNotNull(fileUploadLinkHandler);
        assertSame(feedbackLinkHandler, Deencapsulation.getField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD));
        assertSame(fileUploadLinkHandler, Deencapsulation.getField(connectionHandler, FILE_UPLOAD_LINK_HANDLER_FIELD));
    }

    @Test
    public void onAuthenticationSucceededOpensNewReceiverLinksOnNewConnection()
    {
        // arrange
        EventReceivingConnectionHandler connectionHandler = createConnectionHandler(FILE_UPLOAD_CALLBACK, FEEDBACK_CALLBACK);
        authenticateOnNewConnection(connectionHandler, null);
        Object staleFeedbackLinkHandler = Deencapsulation.getField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD);
        Object staleFileUploadLinkHandler = Deencapsulation.getField(connectionHandler, FILE_UPLOAD_LINK_HANDLER_FIELD);

        // act
        Connection newConnection = authenticateOnNewConnection(connectionHandler, null);

        // assert
        Session session = Deencapsulation.getField(connectionHandler, "session");
        assertSame(newConnection, session.getConnection());
        assertTrue(session.getLocalState() == EndpointState.ACTIVE);
        assertNotSame(staleFeedbackLinkHandler, Deencapsulation.getField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD));
        assertNotSame(staleFileUploadLinkHandler, Deencapsulation.getField(connectionHandler, FILE_UPLOAD_LINK_HANDLER_FIELD));
    }

    @Test
    public void onReactorFinalFromPreviousReactorDoesNotClearNewReceiverLinks(
        @Injectable final Reactor previousReactor,
        @Injectable final Reactor currentReactor)
    {
        // arrange
        EventReceivingConnectionHandler connectionHandler = createConnectionHandler(null, FEEDBACK_CALLBACK);
        authenticateOnNewConnection(connectionHandler, previousReactor);
        authenticateOnNewConnection(connectionHandler, currentReactor);
        Object currentFeedbackLinkHandler = Deencapsulation.getField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD);

        new Expectations()
        {
            {
                mockEvent.getReactor(); returns(previousReactor, currentReactor);
            }
        };

        // act
        connectionHandler.onReactorFinal(mockEvent);

        // assert
        assertSame(currentFeedbackLinkHandler, Deencapsulation.getField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD));

        // act
        connectionHandler.onReactorFinal(mockEvent);

        // assert
        assertNull(Deencapsulation.getField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD));
    }

    private static void mockOpenConnectionAndSession(
        EventReceivingConnectionHandler connectionHandler,
        final Connection connection,
        final CbsSessionHandler cbsSessionHandler,
        final Session session)
    {
        Deencapsulation.setField(connectionHandler, "connection", connection);
        Deencapsulation.setField(connectionHandler, "cbsSessionHandler", cbsSessionHandler);
        Deencapsulation.setField(connectionHandler, "session", session);

        new Expectations()
        {
            {
                connection.getLocalState(); result = EndpointState.ACTIVE; minTimes = 0;
                connection.getRemoteState(); result = EndpointState.ACTIVE; minTimes = 0;
                cbsSessionHandler.isOpen(); result = true; minTimes = 0;
                session.getLocalState(); result = EndpointState.ACTIVE; minTimes = 0;
                session.getRemoteState(); result = EndpointState.ACTIVE; minTimes = 0;
            }
        };
    }

    @Test
    public void isOpenReturnsFalseWhenReceiverLinkWasDetached(
        @Injectable final Connection connection,
        @Injectable final CbsSessionHandler cbsSessionHandler,
        @Injectable final Session session,
        @Injectable final MessageFeedbackReceiverLinkHandler feedbackLinkHandler)
    {
        // arrange
        EventReceivingConnectionHandler connectionHandler = createConnectionHandler(null, FEEDBACK_CALLBACK);
        mockOpenConnectionAndSession(connectionHandler, connection, cbsSessionHandler, session);
        Deencapsulation.setField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD, feedbackLinkHandler);

        new Expectations()
        {
            {
                feedbackLinkHandler.isOpen(); result = false;
            }
        };

        // act/assert
        assertFalse("The client must not report that it is running once its receiver link is gone", connectionHandler.isOpen());
    }

    @Test
    public void isOpenReturnsTrueWhenEveryRequestedReceiverLinkIsOpen(
        @Injectable final Connection connection,
        @Injectable final CbsSessionHandler cbsSessionHandler,
        @Injectable final Session session,
        @Injectable final MessageFeedbackReceiverLinkHandler feedbackLinkHandler)
    {
        // arrange
        // no file upload notification callback was provided, so no file upload notification link is required
        EventReceivingConnectionHandler connectionHandler = createConnectionHandler(null, FEEDBACK_CALLBACK);
        mockOpenConnectionAndSession(connectionHandler, connection, cbsSessionHandler, session);
        Deencapsulation.setField(connectionHandler, FEEDBACK_LINK_HANDLER_FIELD, feedbackLinkHandler);

        new Expectations()
        {
            {
                feedbackLinkHandler.isOpen(); result = true;
            }
        };

        // act/assert
        assertTrue(connectionHandler.isOpen());
    }

    @Test
    public void isOpenReturnsFalseWhenRequestedReceiverLinkWasNeverCreated(
        @Injectable final Connection connection,
        @Injectable final CbsSessionHandler cbsSessionHandler,
        @Injectable final Session session)
    {
        // arrange
        EventReceivingConnectionHandler connectionHandler = createConnectionHandler(FILE_UPLOAD_CALLBACK, null);
        mockOpenConnectionAndSession(connectionHandler, connection, cbsSessionHandler, session);

        // act/assert
        assertFalse(connectionHandler.isOpen());
    }
}
