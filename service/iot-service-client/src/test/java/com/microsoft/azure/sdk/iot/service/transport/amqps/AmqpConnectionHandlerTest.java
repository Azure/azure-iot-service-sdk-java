// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the connection level handling in {@link AmqpConnectionHandler} of links that the service closes or
 * detaches, and of transport errors.
 */
@RunWith(JMockit.class)
public class AmqpConnectionHandlerTest
{
    private static final String LINK_TYPE = "cloudToDeviceSender";

    @Mocked Event mockEvent;
    @Mocked Reactor mockReactor;

    private final List<ErrorContext> reportedErrors = new ArrayList<>();
    private TestAmqpConnectionHandler connectionHandler;
    private Connection connection;
    private Session session;
    private Sender sender;

    @Before
    public void setUp()
    {
        connectionHandler = new TestAmqpConnectionHandler(reportedErrors::add);
        connection = Proton.connection();
        connection.open();
        session = connection.session();
        session.open();
        sender = session.sender("sender");
        sender.open();
    }

    private void recordEventEndpoints(final Link link, final Transport transport)
    {
        new Expectations()
        {
            {
                mockEvent.getConnection(); result = connection; minTimes = 0;
                mockEvent.getSession(); result = session; minTimes = 0;
                mockEvent.getLink(); result = link; minTimes = 0;
                mockEvent.getSender(); result = link instanceof Sender ? link : null; minTimes = 0;
                mockEvent.getReceiver(); result = link instanceof Receiver ? link : null; minTimes = 0;
                mockEvent.getTransport(); result = transport; minTimes = 0;
                mockEvent.getReactor(); result = mockReactor; minTimes = 0;
            }
        };
    }

    @Test
    public void linkDetachedWithoutErrorConditionReportsNetworkErrorAndClosesConnection() throws IotHubException
    {
        // arrange
        recordEventEndpoints(sender, null);

        // act
        connectionHandler.onLinkRemoteClosedUnexpectedly(mockEvent, LINK_TYPE);

        // assert
        assertEquals(1, reportedErrors.size());
        assertNull(reportedErrors.get(0).getIotHubException());
        assertTrue(reportedErrors.get(0).getNetworkException().getMessage().contains(LINK_TYPE));
        assertEquals("The service may keep the connection open, so the client must close it", EndpointState.CLOSED, connection.getLocalState());

        try
        {
            connectionHandler.verifyConnectionWasOpened();
            fail("Expected the link loss to be reported when the reactor stops");
        }
        catch (IOException expected)
        {
            // expected
        }
    }

    @Test
    public void linkDetachedWithErrorConditionReportsMatchingIotHubException() throws IOException
    {
        // arrange
        sender.getRemoteCondition().setCondition(Symbol.valueOf(IotHubUnauthorizedException.amqpErrorCode));
        sender.getRemoteCondition().setDescription("token expired");
        recordEventEndpoints(sender, null);

        // act
        connectionHandler.onLinkRemoteClosedUnexpectedly(mockEvent, LINK_TYPE);

        // assert
        assertEquals(1, reportedErrors.size());
        assertTrue(reportedErrors.get(0).getIotHubException() instanceof IotHubUnauthorizedException);
        assertEquals(EndpointState.CLOSED, connection.getLocalState());

        try
        {
            connectionHandler.verifyConnectionWasOpened();
            fail("Expected the link loss to be reported when the reactor stops");
        }
        catch (IotHubUnauthorizedException expected)
        {
            // expected
        }
        catch (IotHubException e)
        {
            fail("Expected an IotHubUnauthorizedException, but got " + e);
        }
    }

    @Test
    public void linkClosedWhileConnectionIsAlreadyClosingIsNotReported() throws IOException, IotHubException
    {
        // arrange
        connection.close();
        recordEventEndpoints(sender, null);

        // act
        connectionHandler.onLinkRemoteClosedUnexpectedly(mockEvent, LINK_TYPE);

        // assert
        assertEquals(0, reportedErrors.size());
        connectionHandler.verifyConnectionWasOpened();
    }

    @Test
    public void onReactorInitClearsErrorFromPreviousConnection() throws IOException, IotHubException
    {
        // arrange
        recordEventEndpoints(sender, null);
        connectionHandler.onLinkRemoteClosedUnexpectedly(mockEvent, LINK_TYPE);

        // act
        connectionHandler.callSuperOnReactorInit(mockEvent);

        // assert
        connectionHandler.verifyConnectionWasOpened();
        new Verifications()
        {
            {
                mockReactor.connectionToHost("aaa.bbb.ccc", anyInt, connectionHandler);
                times = 1;
            }
        };
    }

    @Test
    public void transportErrorWhileConnectionIsOpenIsReportedAndClosesConnection()
    {
        // arrange
        Transport transport = Proton.transport();
        transport.setCondition(new ErrorCondition(Symbol.valueOf("proton:io"), "connection reset"));
        recordEventEndpoints(null, transport);

        // act
        connectionHandler.onTransportError(mockEvent);

        // assert
        assertEquals(1, reportedErrors.size());
        assertTrue(reportedErrors.get(0).getNetworkException() != null);
        assertEquals(EndpointState.CLOSED, connection.getLocalState());
    }

    @Test
    public void transportErrorAfterConnectionWasClosedLocallyIsNotReported() throws IOException, IotHubException
    {
        // arrange
        connection.close();
        Transport transport = Proton.transport();
        transport.setCondition(new ErrorCondition(Symbol.valueOf("amqp:connection:framing-error"), "connection aborted"));
        recordEventEndpoints(null, transport);

        // act
        connectionHandler.onTransportError(mockEvent);

        // assert
        assertEquals(0, reportedErrors.size());
        connectionHandler.verifyConnectionWasOpened();
    }

    @Test
    public void onTransportClosedStopsReactor()
    {
        // arrange
        recordEventEndpoints(null, null);

        // act
        connectionHandler.onTransportClosed(mockEvent);

        // assert
        new Verifications()
        {
            {
                mockReactor.stop();
                times = 1;
            }
        };
    }
}
