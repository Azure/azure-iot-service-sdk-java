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

/**
 * Implementation of {@link HttpPipelinePolicy} that provides RBAC based bearer tokens.
 */
@AllArgsConstructor
public class ServiceClientBearerTokenCredentialProvider implements HttpPipelinePolicy {

    private static final String AUTHORIZATION = "Authorization";

    @NonNull
    private final BearerTokenProvider tokenProvider;

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getHttpRequest().setHeader(AUTHORIZATION, tokenProvider.getBearerToken());
        return next.process();
    }
}
