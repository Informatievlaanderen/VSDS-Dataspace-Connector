# VSDS Dataspace connector

This repository provides a connector build that can be used to create a dataspace with an LDES Server.

To create the image, run the following docker command from the root directory:

1. Image for consumer and provider connectors
    ```bash
    docker build -t vsds-dataspace-connector:local . -f ./http-plane-headers-extension/Dockerfile
    ```
   
   > **Note**: This connector is not production ready and should only be used for development purposes.

3. E2E testing
   - [Basic flow](https://github.com/Informatievlaanderen/VSDS-LDES-E2E-testing/tree/main/tests/034.dataspace-connector-consumer-and-provider)

## Next steps:

- Support DAPS or OAUTH2 as a production suitable alternative for the current dev build which uses the mock.iam
- Setup persistence strategy to replace in memory catalog causing the config to reset upon restarts
- Provide UI container for easier setup using [sovity](https://github.com/sovity/edc-ui/tree/v2.0.0) or [eclipse-edc](https://github.com/eclipse-edc/DataDashboard)
