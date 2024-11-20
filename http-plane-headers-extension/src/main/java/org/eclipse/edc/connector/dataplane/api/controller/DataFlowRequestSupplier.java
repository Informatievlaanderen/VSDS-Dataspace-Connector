package org.eclipse.edc.connector.dataplane.api.controller;

import org.eclipse.edc.connector.dataplane.api.pipeline.ApiDataSinkFactory;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.eclipse.edc.connector.dataplane.api.pipeline.HttpPart.excludedHeaders;
import static org.eclipse.edc.connector.dataplane.spi.schema.DataFlowRequestSchema.*;

public class DataFlowRequestSupplier implements BiFunction<org.eclipse.edc.connector.dataplane.api.controller.ContainerRequestContextApi, DataAddress, DataFlowStartMessage> {

    /**
     * Create a {@link DataFlowStartMessage} based on incoming request and claims decoded from the access token.
     *
     * @param contextApi  Api for accessing request properties.
     * @param dataAddress Source data address.
     * @return DataFlowRequest
     */
    @Override
    public DataFlowStartMessage apply(org.eclipse.edc.connector.dataplane.api.controller.ContainerRequestContextApi contextApi, DataAddress dataAddress) {
        var props = createProps(contextApi);
        return DataFlowStartMessage.Builder.newInstance()
                .processId(UUID.randomUUID().toString())
                .sourceDataAddress(dataAddress)
                .destinationDataAddress(DataAddress.Builder.newInstance()
                        .type(ApiDataSinkFactory.TYPE)
                        .build())
                .id(UUID.randomUUID().toString())
                .properties(props)
                .build();
    }

    /**
     * Put all properties of the incoming request (method, request body, query params...) into a map.
     */
    private static Map<String, String> createProps(ContainerRequestContextApi contextApi) {
        var props = new HashMap<String, String>();
        props.put(METHOD, contextApi.method());
        props.put(QUERY_PARAMS, contextApi.queryParams());
        props.put(PATH, contextApi.path());
        Optional.ofNullable(contextApi.mediaType())
                .ifPresent(mediaType -> {
                    props.put(MEDIA_TYPE, mediaType);
                    props.put(BODY, contextApi.body());
                });
        contextApi.headers().forEach((key, value) -> {
            if (isAdditionalHeader(key)) {
                props.put(key, value);
            }
        });
        return props;
    }

    private static boolean isAdditionalHeader(String key) {
        return !excludedHeaders.contains(key);

    }
}
