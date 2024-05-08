## Changes from source project

Part of this project uses a modified version of the public api extension and http extension of the edc-connector from: https://github.com/eclipse-edc/Connector

The following files were modified from their original source:
- http-plane-headers-extension/src/main/java/org/eclipse/org/eclipse/edc/connector/dataplane/api/controller/ContainerRequestContextApiImpl.java
- http-plane-headers-extension/src/main/java/org/eclipse/org/eclipse/edc/connector/dataplane/api/controller/DataFlowRequestSupplier.java
- http-plane-headers-extension/src/main/java/org/eclipse/edc/connector/dataplane/api/controller/ExtendedDataPlanePublicApiController.java
- http-plane-headers-extension/src/main/java/org/eclipse/edc/connector/dataplane/api/pipeline/ApiDataSink.java
- http-plane-headers-extension/src/main/java/org/eclipse/edc/connector/dataplane/api/pipeline/ApiDataSinkFactory.java
- http-plane-headers-extension/src/main/java/org/eclipse/edc/connector/dataplane/api/DataPlanePublicApiExtension.java
- http-plane-headers-extension/src/main/java/org/eclipse/edc/connector/dataplane/http/pipeline/datasink/HttpDataSink.java
- http-plane-headers-extension/src/main/java/org/eclipse/edc/connector/dataplane/http/pipeline/datasource/HttpDataSource.java
- http-plane-headers-extension/src/main/java/org/eclipse/edc/connector/dataplane/http/DataPlaneHttpExtension.java