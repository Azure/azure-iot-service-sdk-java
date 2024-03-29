// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

import java.io.IOException;

public interface SasTokenProvider {
    String getSasToken() throws IOException;
}
