package org.eclipse.edc.connector.dataplane.http.pipeline;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Writes content into an OK HTTP buffered sink.
 *
 * @see <a href="https://github.com/square/okhttp/blob/master/docs/features/calls.md">OkHttp Dcoumentation</a>
 */
public abstract class AbstractTransferRequestBody extends RequestBody {

    private final String contentType;

    protected AbstractTransferRequestBody(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }
}
