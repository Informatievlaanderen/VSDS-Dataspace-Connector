package org.eclipse.edc.connector.dataplane.http.pipeline;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Supplier for string request body.
 */
public class StringRequestBodySupplier implements Supplier<InputStream> {

    private final String body;

    public StringRequestBodySupplier(String requestBody) {
        Objects.requireNonNull(requestBody);
        this.body = requestBody;
    }

    @Override
    public InputStream get() {
        return new ByteArrayInputStream(body.getBytes());
    }
}
