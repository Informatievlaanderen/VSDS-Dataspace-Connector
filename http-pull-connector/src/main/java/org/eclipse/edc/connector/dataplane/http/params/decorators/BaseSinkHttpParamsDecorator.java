package org.eclipse.edc.connector.dataplane.http.params.decorators;

import org.eclipse.edc.connector.dataplane.http.spi.HttpDataAddress;
import org.eclipse.edc.connector.dataplane.http.spi.HttpParamsDecorator;
import org.eclipse.edc.connector.dataplane.http.spi.HttpRequestParams;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowRequest;

import java.util.Optional;

public class BaseSinkHttpParamsDecorator implements HttpParamsDecorator {
    private static final String DEFAULT_METHOD = "POST";

    @Override
    public HttpRequestParams.Builder decorate(DataFlowRequest request, HttpDataAddress address, HttpRequestParams.Builder params) {
        var method = Optional.ofNullable(address.getMethod()).orElse(DEFAULT_METHOD);
        params.method(method);
        params.path(address.getPath());
        params.queryParams(null);
        Optional.ofNullable(address.getContentType())
                .ifPresent(contentType -> {
                    params.contentType(contentType);
                    params.body(null);
                });
        params.nonChunkedTransfer(address.getNonChunkedTransfer());
        return params;
    }

}
