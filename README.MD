# VSDS Dataspace connector

> ⚠️ **Warning!**
>
> **This page has been archived. All documentation, source code, Docker images, and release information for the LDES software have been moved to the open-source project at [openldes.org](https://openldes.org).**
>
> **This page was originally created for the completed VSDS project, funded under the European Recovery and Resilience Facility (RRF) as part of NextGenerationEU and aligned with EU digital transformation strategies.**
>
> You can find the new source code at: https://github.com/orgs/OpenLDES/repositories 


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
