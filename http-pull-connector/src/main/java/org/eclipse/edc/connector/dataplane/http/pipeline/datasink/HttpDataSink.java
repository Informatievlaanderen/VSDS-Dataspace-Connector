package org.eclipse.edc.connector.dataplane.http.pipeline.datasink;

import org.eclipse.edc.connector.dataplane.api.pipeline.HttpPart;
import org.eclipse.edc.connector.dataplane.http.params.HttpRequestFactory;
import org.eclipse.edc.connector.dataplane.http.spi.HttpRequestParams;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.StreamFailure;
import org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult;
import org.eclipse.edc.connector.dataplane.util.sink.ParallelSink;
import org.eclipse.edc.spi.http.EdcHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Writes data in a streaming fashion to an HTTP endpoint.
 */
public class HttpDataSink extends ParallelSink {
    private static final StreamResult<Object> ERROR_WRITING_DATA = StreamResult.error("Error writing data");

    private HttpRequestParams params;
    private EdcHttpClient httpClient;
    private HttpRequestFactory requestFactory;

    @Override
    protected StreamResult<Object> transferParts(List<DataSource.Part> parts) {
        for (var part : parts) {
            Map<String, String> additionalHeaders = Map.of();
            if (part instanceof HttpPart httpPart) {
                if (!httpPart.isOk()) {
                    return failingStatusCodeToStreamResult(httpPart);
                }
                additionalHeaders = httpPart.getHeadersForRequest();
            }

            var request = requestFactory.toRequest(params, additionalHeaders, part::openStream);
            try (var response = httpClient.execute(request)) {
                if (!response.isSuccessful()) {
                    monitor.severe(format("Error {%s: %s} received writing HTTP data %s to endpoint %s for request: %s",
                            response.code(), response.message(), part.name(), request.url().url(), request));
                    return ERROR_WRITING_DATA;
                }
            } catch (Exception e) {
                monitor.severe(format("Error writing HTTP data %s to endpoint %s for request: %s", part.name(), request.url().url(), request), e);
                return ERROR_WRITING_DATA;
            }
        }
        return StreamResult.success();
    }

    private StreamResult<Object> failingStatusCodeToStreamResult(HttpPart httpPart) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(httpPart.openStream()))){
            String message = reader.lines().parallel().collect(Collectors.joining("\n"));
            return switch (httpPart.getStatusCode()) {
                case 401, 403 -> StreamResult.notAuthorized();
                case 404 -> StreamResult.notFound();
                default ->
                        StreamResult.failure(new StreamFailure(List.of(message), StreamFailure.Reason.GENERAL_ERROR));
            };
        } catch (IOException e) {
            monitor.severe(format("Error writing error message of %s", httpPart.name()), e);
            return ERROR_WRITING_DATA;
        }
    }

    private HttpDataSink() {
    }

    public static class Builder extends ParallelSink.Builder<Builder, HttpDataSink> {

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {
            super(new HttpDataSink());
        }

        public Builder params(HttpRequestParams params) {
            sink.params = params;
            return this;
        }

        public Builder httpClient(EdcHttpClient httpClient) {
            sink.httpClient = httpClient;
            return this;
        }

        public Builder requestFactory(HttpRequestFactory requestFactory) {
            sink.requestFactory = requestFactory;
            return this;
        }

        @Override
        protected void validate() {
            Objects.requireNonNull(sink.requestFactory, "requestFactory");
        }
    }
}
