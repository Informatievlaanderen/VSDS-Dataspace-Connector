package org.eclipse.edc.connector.dataplane.api.pipeline;

import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpPart implements DataSource.Part {
    public static final List<String> excludedHeaders = List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT);
    private final String name;
    private final Map<String, List<String>> headers;
    private final int statusCode;
    private final InputStream content;

    public HttpPart(String name, Map<String, List<String>> headers, int statusCode, InputStream content) {
        this.name = name;
        this.headers = headers;
        this.statusCode = statusCode;
        this.content = content;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long size() {
        return SIZE_UNKNOWN;
    }

    @Override
    public InputStream openStream() {
        return content;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, String> getHeadersForRequest() {
        Map<String, String> requestHeadersMap = new HashMap<>();
        headers.forEach((key, value) -> {
            if (isRequestHeader(key)) {
                requestHeadersMap.put(key, String.join(",", value));
            }
        });
        return requestHeadersMap;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isOk() {
        return 200 <= statusCode && statusCode < 300;
    }

    private boolean isRequestHeader(String key) {
        return !excludedHeaders.contains(key);
    }
}