// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AccessToken;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the proactive token renewal and link loss handling of {@link CbsSessionHandler}.
 */
@RunWith(JMockit.class)
public class CbsSessionHandlerTest
{
    private static final long ONE_HOUR_MILLISECONDS = 60 * 60 * 1000;

    @Injectable Session mockSession;
    @Injectable Connection mockConnection;
    @Injectable Reactor mockReactor;
    @Injectable CbsSenderLinkHandler mockCbsSenderLinkHandler;
    @Injectable CbsReceiverLinkHandler mockCbsReceiverLinkHandler;
    @Injectable CbsSessionStateCallback mockCbsSessionStateCallback;
    @Mocked Event mockEvent;

    private CbsSessionHandler cbsSessionHandler;

    @Before
    public void setUp()
    {
        cbsSessionHandler = new CbsSessionHandler(mockSession, mockCbsSessionStateCallback, TestAmqpConnectionHandler.CONNECTION_STRING);
        Deencapsulation.setField(cbsSessionHandler, "cbsSenderLinkHandler", mockCbsSenderLinkHandler);
        Deencapsulation.setField(cbsSessionHandler, "cbsReceiverLinkHandler", mockCbsReceiverLinkHandler);

        new Expectations()
        {
            {
                mockCbsSenderLinkHandler.sendAuthenticationMessage((UUID) any); result = 1; minTimes = 0;
                mockCbsSenderLinkHandler.getCurrentAccessToken(); result = new AccessToken("token", OffsetDateTime.now().plusHours(1)); minTimes = 0;
                mockSession.getConnection(); result = mockConnection; minTimes = 0;
                mockConnection.getReactor(); result = mockReactor; minTimes = 0;
            }
        };
    }

    @Test
    public void connectionStringAuthenticationSchedulesProactiveTokenRenewal()
    {
        // act
        cbsSessionHandler.onSenderLinkRemoteOpen();
        cbsSessionHandler.onReceiverLinkRemoteOpen();

        // assert
        new Verifications()
        {
            {
                int renewalDelayMilliseconds;
                mockReactor.schedule(renewalDelayMilliseconds = withCapture(), cbsSessionHandler);
                times = 1;

                // The SAS token is valid for one hour, so it should be renewed after roughly 85% of that time
                assertTrue("Unexpected renewal delay " + renewalDelayMilliseconds,
                    renewalDelayMilliseconds > 0.80 * ONE_HOUR_MILLISECONDS
                        && renewalDelayMilliseconds < 0.86 * ONE_HOUR_MILLISECONDS);
            }
        };
    }

    @Test
    public void connectionStringAuthenticationSendsNewTokenWhenRenewalTimerFires()
    {
        // arrange
        cbsSessionHandler.onSenderLinkRemoteOpen();
        cbsSessionHandler.onReceiverLinkRemoteOpen();

        // act
        cbsSessionHandler.onTimerTask(mockEvent);

        // assert
        new Verifications()
        {
            {
                mockCbsSenderLinkHandler.sendAuthenticationMessage((UUID) any);
                times = 2;
                mockReactor.schedule(anyInt, cbsSessionHandler);
                times = 2;
            }
        };
    }

    @Test
    public void onLinkRemoteClosedUnexpectedlyIsForwardedToConnectionHandler()
    {
        // act
        cbsSessionHandler.onLinkRemoteClosedUnexpectedly(mockEvent, "cbs");

        // assert
        new Verifications()
        {
            {
                mockCbsSessionStateCallback.onLinkRemoteClosedUnexpectedly(mockEvent, "cbs");
                times = 1;
            }
        };
    }
}
