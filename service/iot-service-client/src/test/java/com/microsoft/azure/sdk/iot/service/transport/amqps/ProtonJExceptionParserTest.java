// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ProtonJExceptionParser}.
 */
public class ProtonJExceptionParserTest
{
    @Test
    public void parserWithoutErrorConditionHasNoExceptions()
    {
        // The service can detach a link without an error condition, which must not be treated as a parsing failure
        ProtonJExceptionParser parser = new ProtonJExceptionParser((String) null, null);

        assertNull(parser.getIotHubException());
        assertNull(parser.getNetworkException());
    }

    @Test
    public void parserMapsUnauthorizedErrorCondition()
    {
        ProtonJExceptionParser parser = new ProtonJExceptionParser(IotHubUnauthorizedException.amqpErrorCode, "token expired");

        assertTrue(parser.getIotHubException() instanceof IotHubUnauthorizedException);
        assertNull(parser.getNetworkException());
    }

    @Test
    public void parserMapsProtonIoErrorConditionToNetworkException()
    {
        ProtonJExceptionParser parser = new ProtonJExceptionParser("proton:io", "connection reset");

        assertNull(parser.getIotHubException());
        assertTrue(parser.getNetworkException() != null);
    }

    @Test
    public void parserWrapsProvidedNetworkException()
    {
        IOException networkException = new IOException("link detached");

        ProtonJExceptionParser parser = new ProtonJExceptionParser(networkException);

        assertSame(networkException, parser.getNetworkException());
        assertNull(parser.getIotHubException());
    }
}
