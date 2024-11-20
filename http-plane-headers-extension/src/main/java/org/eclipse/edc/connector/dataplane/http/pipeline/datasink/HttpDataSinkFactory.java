package org.eclipse.edc.connector.dataplane.http.pipeline.datasink;

import org.eclipse.edc.connector.dataplane.http.params.HttpRequestFactory;
import org.eclipse.edc.connector.dataplane.http.spi.HttpRequestParamsProvider;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSink;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSinkFactory;
import org.eclipse.edc.dataaddress.httpdata.spi.HttpDataAddressSchema;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

import static org.eclipse.edc.dataaddress.httpdata.spi.HttpDataAddressSchema.HTTP_DATA_TYPE;

/**
 * Instantiates {@link HttpDataSink}s for requests whose source data type is {@link HttpDataAddressSchema#HTTP_DATA_TYPE}.
 */
public class HttpDataSinkFactory implements DataSinkFactory {
    private final EdcHttpClient httpClient;
    private final ExecutorService executorService;
    private final int partitionSize;
    private final Monitor monitor;
    private final HttpRequestParamsProvider requestParamsProvider;
    private final HttpRequestFactory requestFactory;

    public HttpDataSinkFactory(EdcHttpClient httpClient,
                               ExecutorService executorService,
                               int partitionSize,
                               Monitor monitor,
                               HttpRequestParamsProvider requestParamsProvider, HttpRequestFactory requestFactory) {
        this.httpClient = httpClient;
        this.executorService = executorService;
        this.partitionSize = partitionSize;
        this.monitor = monitor;
        this.requestParamsProvider = requestParamsProvider;
        this.requestFactory = requestFactory;
    }

    @Override
    public String supportedType() {
        return HTTP_DATA_TYPE;
    }

    @Override
    public DataSink createSink(DataFlowStartMessage dataFlowStartMessage) {
        return HttpDataSink.Builder.newInstance()
                .params(requestParamsProvider.provideSinkParams(dataFlowStartMessage))
                .requestId(dataFlowStartMessage.getId())
                .partitionSize(partitionSize)
                .httpClient(httpClient)
                .executorService(executorService)
                .monitor(monitor)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage dataFlowStartMessage) {
        try {
            createSink(dataFlowStartMessage);
        } catch (Exception e) {
            return Result.failure("Failed to build HttpDataSink: " + e.getMessage());
        }
        return Result.success();
    }
}
