/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Mercedes Benz Tech Innovation - add toggles for proxy behavior
 *
 */

package org.eclipse.edc.connector.dataplane.http.pipeline.datasource;

import org.eclipse.edc.connector.dataplane.http.params.HttpRequestFactory;
import org.eclipse.edc.connector.dataplane.http.spi.HttpDataAddress;
import org.eclipse.edc.connector.dataplane.http.spi.HttpRequestParamsProvider;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.dataaddress.httpdata.spi.HttpDataAddressSchema;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowRequest;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.edc.dataaddress.httpdata.spi.HttpDataAddressSchema.HTTP_DATA_TYPE;


/**
 * Instantiates {@link HttpDataSourceFactory}s for requests whose source data type is {@link HttpDataAddressSchema#HTTP_DATA_TYPE}.
 */
public class HttpDataSourceFactory implements DataSourceFactory {
    private final EdcHttpClient httpClient;
    private final HttpRequestParamsProvider requestParamsProvider;
    private final Monitor monitor;
    private final HttpRequestFactory requestFactory;

    public HttpDataSourceFactory(EdcHttpClient httpClient, HttpRequestParamsProvider requestParamsProvider, Monitor monitor, HttpRequestFactory requestFactory) {
        this.httpClient = httpClient;
        this.requestParamsProvider = requestParamsProvider;
        this.monitor = monitor;
        this.requestFactory = requestFactory;
    }

    @Override
    public boolean canHandle(DataFlowRequest request) {
        return HTTP_DATA_TYPE.equals(request.getSourceDataAddress().getType());
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowRequest request) {
        try {
            createSource(request);
        } catch (Exception e) {
            return Result.failure("Failed to build ExtendedHttpDataSource: " + e.getMessage());
        }
        return Result.success();
    }

    @Override
    public DataSource createSource(DataFlowRequest request) {
        var dataAddress = HttpDataAddress.Builder.newInstance()
                .copyFrom(request.getSourceDataAddress())
                .build();
        return HttpDataSource.Builder.newInstance()
                .httpClient(httpClient)
                .monitor(monitor)
                .requestId(request.getId())
                .name(dataAddress.getName())
                .params(requestParamsProvider.provideSourceParams(request))
                .requestFactory(requestFactory)
                .build();
    }
}
