/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReactorRunner
{
    private final static String THREAD_NAME = "azure-iot-sdk-ReactorRunner";
    private final String threadName;
    private final Reactor reactor;
    private static final int REACTOR_TIMEOUT = 3141; // reactor timeout in milliseconds
    private static final int MAX_FRAME_SIZE = 4 * 1024;
    private final AmqpConnectionHandler handler;

    // Counted down once the connection has closed gracefully or the reactor thread has finished, whichever happens first
    private final CountDownLatch reactorClosedLatch = new CountDownLatch(1);

    public ReactorRunner(AmqpConnectionHandler handler) throws IOException
    {
        this(null, null, handler);
    }

    public ReactorRunner(String threadNamePrefix, String threadNamePostfix, AmqpConnectionHandler handler) throws IOException
    {
        ReactorOptions options = new ReactorOptions();

        // If this option isn't set, proton defaults to 16 * 1024 max frame size. This used to default to 4 * 1024,
        // and this change to 16 * 1024 broke the websocket implementation that we layer on top of proton-j.
        // By setting this frame size back to 4 * 1024, AMQPS_WS clients can send messages with payloads up to the
        // expected 64 * 1024 bytes. For more context, see https://github.com/Azure/azure-iot-sdk-java/issues/742
        options.setMaxFrameSize(MAX_FRAME_SIZE);

        this.reactor = Proton.reactor(options, handler);
        this.threadName = threadNamePrefix + "-" + THREAD_NAME + "-" + threadNamePostfix;
        this.handler = handler;
    }

    public void run()
    {
        if (this.threadName != null)
        {
            Thread.currentThread().setName(this.threadName);
        }

        try
        {
            log.trace("Starting reactor thread {}", this.threadName);
            this.reactor.setTimeout(REACTOR_TIMEOUT);
            this.reactor.run();
        }
        catch (HandlerException e)
        {
            log.debug("Encountered an exception while running reactor on thread {}", threadName, e);
        }
        finally
        {
            log.trace("Freeing reactor now that reactor thread is done");
            this.reactor.free();
            this.reactorClosedLatch.countDown();
        }

        log.trace("Finished reactor thread {}", this.threadName);
    }

    public void stop(int timeoutMilliseconds) throws InterruptedException
    {
        if (timeoutMilliseconds < 0)
        {
            throw new IllegalArgumentException("timeoutMilliseconds must greater than or equal to 0");
        }

        if (this.reactorClosedLatch.getCount() == 0)
        {
            // The reactor already stopped on its own, such as after the service closed the connection. There is
            // no connection left to close gracefully, and waiting for one to close would only block until the timeout.
            log.trace("Reactor thread {} has already finished, so there is nothing to stop", this.threadName);
            return;
        }

        try
        {
            // The connection may already be closing (for instance, after the service detached its links), in which
            // case the close callback may never run. The reactor thread finishing also releases this latch.
            this.handler.closeAsync(this.reactorClosedLatch::countDown);

            if (timeoutMilliseconds > 0)
            {
                boolean timedOut = !this.reactorClosedLatch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

                if (timedOut)
                {
                    log.debug("Timed out waiting for amqp connection to close gracefully. Closing forcefully now.");
                }
            }
            else
            {
                this.reactorClosedLatch.await(); // wait indefinitely
            }
        }
        finally
        {
            this.reactor.stop();
        }
    }

    public boolean isRunning()
    {
        return this.reactor != null && this.handler != null && this.handler.isOpen();
    }
}

