# VSDS Dataspace connector

This repository provides connector builds that can be used to create a dataspace with an LDES Server.

To create the images run the following docker commands from the root directory:

1. Image for consumer and provider connectors
    ```bash
    docker build -t vsds-dataspace-connector . -f ./http-pull-connector/Dockerfile
    ```

2. Image for the federated catalog connector
    ```bash
    docker build -t vsds-federated-catalog-connector . -f ./federated-catalog-connector/Dockerfile
    ```

> **Note**: These connectors are not production ready and should only be used for development purposes.

## Next steps:

- Support DAPS or OAUTH2 as a production suitable alternative for the current dev build which uses the mock.iam
- Setup persistence strategy to replace in memory catalog causing the config to reset upon restarts
- Provide UI container for easier setup using [sovity](https://github.com/sovity/edc-ui/tree/v2.0.0) or [eclipse-edc](https://github.com/eclipse-edc/DataDashboard)
