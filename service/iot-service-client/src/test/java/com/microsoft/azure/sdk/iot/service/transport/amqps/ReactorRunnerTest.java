// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ReactorRunner}. These run a real proton-j reactor that never opens a network connection.
 */
public class ReactorRunnerTest
{
    private static final int STOP_TIMEOUT_MILLISECONDS = 30 * 1000;

    @Test (timeout = 20 * 1000)
    public void stopReturnsImmediatelyWhenReactorAlreadyFinished() throws IOException, InterruptedException
    {
        // arrange
        TestAmqpConnectionHandler connectionHandler = new TestAmqpConnectionHandler(null);
        ReactorRunner reactorRunner = new ReactorRunner(connectionHandler);

        // With no connection and no scheduled tasks, the reactor finishes on its own, like after a lost connection
        reactorRunner.run();

        // act
        long startTime = System.currentTimeMillis();
        reactorRunner.stop(STOP_TIMEOUT_MILLISECONDS);
        long elapsedMilliseconds = System.currentTimeMillis() - startTime;

        // assert
        assertTrue("stop() waited " + elapsedMilliseconds + "ms for a reactor that had already finished", elapsedMilliseconds < 5000);
        assertEquals("There is no connection left to close", 0, connectionHandler.closeAsyncCallCount);
        assertFalse(reactorRunner.isRunning());
    }

    @Test (timeout = 20 * 1000)
    public void stopReturnsOnceReactorFinishesEvenIfConnectionNeverReportsClosing() throws IOException, InterruptedException
    {
        // arrange
        TestAmqpConnectionHandler connectionHandler = new TestAmqpConnectionHandler(null);
        CountDownLatch reactorStartedLatch = new CountDownLatch(1);

        // Keep the reactor alive briefly, then let it stop on its own without ever running the close callback
        connectionHandler.onReactorInitAction = event ->
        {
            event.getReactor().schedule(1000, connectionHandler);
            reactorStartedLatch.countDown();
        };
        connectionHandler.onTimerTaskAction = event -> event.getReactor().stop();

        ReactorRunner reactorRunner = new ReactorRunner(connectionHandler);
        Thread reactorThread = new Thread(reactorRunner::run);
        reactorThread.start();
        assertTrue(reactorStartedLatch.await(10, TimeUnit.SECONDS));

        // act
        long startTime = System.currentTimeMillis();
        reactorRunner.stop(STOP_TIMEOUT_MILLISECONDS);
        long elapsedMilliseconds = System.currentTimeMillis() - startTime;

        // assert
        assertEquals(1, connectionHandler.closeAsyncCallCount);
        assertTrue("stop() should return once the reactor finishes rather than waiting for the full timeout, but waited " + elapsedMilliseconds + "ms",
            elapsedMilliseconds < 10 * 1000);
        reactorThread.join(5000);
        assertFalse(reactorThread.isAlive());
    }
}
