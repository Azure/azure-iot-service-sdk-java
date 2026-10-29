// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import reactor.core.publisher.Mono;

import java.io.IOException;

@AllArgsConstructor
public class ServiceClientCredentialsProvider implements HttpPipelinePolicy {

    private static final String AUTHORIZATION = "Authorization";
    @NonNull
    private final SasTokenProvider sasTokenProvider;

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        try {
            context.getHttpRequest().setHeader(AUTHORIZATION, sasTokenProvider.getSasToken());
        } catch (IOException e) {
            return Mono.error(e);
        }
        return next.process();
    }
}
