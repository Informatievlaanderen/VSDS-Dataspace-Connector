# VSDS Dataspace connector

This repository provides a connector build that can be used to create a dataspace with an LDES Server.

To create the image run the following docker command from the root directory:

```bash
#docker build -t vsds-dataspace-connector . -f ./http-pull-connector/Dockerfile
docker build -t vsds-federated-catalog-connector . -f ./federated-catalog-connector/Dockerfile
```

> **Note**: These connectors are not production ready and should only be used for development purposes.

## Next steps:

- Support DAPS or OAUTH2 as a production suitable alternative for the current dev build which uses the mock.iam
- Setup persistence strategy to replace in memory catalog causing the config to reset upon restarts
- Provide UI container for easier setup using [sovity](https://github.com/sovity/edc-ui/tree/v2.0.0) or [eclipse-edc](https://github.com/eclipse-edc/DataDashboard)
