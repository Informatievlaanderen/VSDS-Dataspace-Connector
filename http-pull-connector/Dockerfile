# Build the jar file
FROM openjdk:18-ea-bullseye AS build

WORKDIR /app
COPY ./ .
RUN ./gradlew clean build

# Run the jar file
FROM openjdk:18-ea-bullseye
WORKDIR /app
COPY --from=build /app/http-pull-connector/build/libs/pull-connector.jar .

# Specify the command to run your application
CMD java -Dedc.keystore="$EDC_KEYSTORE" -Dedc.keystore.password="$EDC_KEYSTORE_PASSWORD" -Dedc.vault="$EDC_VAULT" -Dedc.fs.config="$EDC_FS_CONFIG" -jar /app/pull-connector.jar
