// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import org.apache.qpid.proton.engine.Event;

import java.util.function.Consumer;

/**
 * Minimal concrete {@link AmqpConnectionHandler} so that the shared connection handling logic can be tested directly.
 * By default, it does not open any network connection when its reactor starts.
 */
class TestAmqpConnectionHandler extends AmqpConnectionHandler
{
    static final String CONNECTION_STRING = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";

    int closeAsyncCallCount = 0;
    Consumer<Event> onReactorInitAction = event -> { };
    Consumer<Event> onTimerTaskAction = event -> { };

    TestAmqpConnectionHandler(Consumer<ErrorContext> errorProcessor)
    {
        super(CONNECTION_STRING, IotHubServiceClientProtocol.AMQPS, errorProcessor, null, null, 230);
    }

    void callSuperOnReactorInit(Event event)
    {
        super.onReactorInit(event);
    }

    @Override
    public void onReactorInit(Event event)
    {
        this.onReactorInitAction.accept(event);
    }

    @Override
    public void onTimerTask(Event event)
    {
        this.onTimerTaskAction.accept(event);
    }

    @Override
    public void closeAsync(Runnable onConnectionClosedCallback)
    {
        // Intentionally never runs the callback, like a connection that is already closing and won't report closing again
        this.closeAsyncCallCount++;
    }

    @Override
    public void onAuthenticationSucceeded()
    {
        // no links to open
    }
}
