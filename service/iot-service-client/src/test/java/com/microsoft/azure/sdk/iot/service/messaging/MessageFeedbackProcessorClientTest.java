// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.transport.amqps.EventReceivingConnectionHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;

/**
 * Unit tests for {@link MessageFeedbackProcessorClient}.
 */
public class MessageFeedbackProcessorClientTest
{
    private static final String CONNECTION_STRING = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

    @Mocked EventReceivingConnectionHandler mockEventReceivingConnectionHandler;
    @Mocked ReactorRunner mockReactorRunner;

    @Test
    public void startReleasesPreviousConnectionThatWasLostBeforeOpeningNewOne() throws Exception
    {
        // Arrange
        MessageFeedbackProcessorClient client = new MessageFeedbackProcessorClient(
            CONNECTION_STRING, IotHubServiceClientProtocol.AMQPS, feedbackBatch -> AcknowledgementType.COMPLETE);
        Deencapsulation.setField(client, "reactorRunner", mockReactorRunner);
        new Expectations()
        {
            {
                // the previous connection was lost, so the client is no longer running
                mockReactorRunner.isRunning(); result = false;
            }
        };

        // Act
        client.start(10000);

        // Assert
        new Verifications()
        {
            {
                mockReactorRunner.stop(anyInt);
                times = 1;
            }
        };
    }
}
