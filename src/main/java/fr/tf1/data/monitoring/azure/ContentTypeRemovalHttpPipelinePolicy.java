package fr.tf1.data.monitoring.azure;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * https://github.com/Azure/azure-sdk-for-java/issues/7234#issuecomment-577905820
 */
final class ContentTypeRemovalHttpPipelinePolicy implements HttpPipelinePolicy {

    static final ContentTypeRemovalHttpPipelinePolicy INSTANCE = new ContentTypeRemovalHttpPipelinePolicy();

    private ContentTypeRemovalHttpPipelinePolicy() {
        // NOP
    }

    @Override
    public Mono<HttpResponse> process(final HttpPipelineCallContext callContext, final HttpPipelineNextPolicy nextPolicy) {
        final HttpHeaders headers = callContext.getHttpRequest().getHeaders();
        final String contentType = headers.getValue("Content-Type");

        if (contentType == null) {
            callContext.getHttpRequest().setHeader("Content-Type", "");
        }

        return nextPolicy.process();
    }
}
