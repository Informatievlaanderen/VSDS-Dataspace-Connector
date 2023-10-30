# Build the jar file
FROM openjdk:18-ea-bullseye AS build

WORKDIR /app
COPY ./ .
RUN ./gradlew clean build
# RUN mv http-pull-connector/build/libs/pull-connector.jar app.jar

# Run the jar file
FROM openjdk:18-ea-bullseye
WORKDIR /app
COPY --from=build /app/http-pull-connector/build/libs/pull-connector.jar .
#COPY --from=build /app/pull-connector.jar

# Set environment variables
#ENV EDC_KEYSTORE=tmp/certs/cert.pfx
#ENV EDC_KEYSTORE_PASSWORD=123456
#ENV EDC_VAULT=tmp/http-pull-provider/provider-vault.properties
#ENV EDC_FS_CONFIG=tmp/http-pull-provider/provider-configuration.properties

# Copy the JAR file into the image

# Specify the command to run your application
#CMD ["java", "-Dedc.keystore=$EDC_KEYSTORE", "-Dedc.keystore.password=$EDC_KEYSTORE_PASSWORD", "-Dedc.vault=$EDC_VAULT", "-Dedc.fs.config=$EDC_FS_CONFIG", "-jar", "/app/pull-connector.jar"]
CMD java -Dedc.keystore=$EDC_KEYSTORE -Dedc.keystore.password=$EDC_KEYSTORE_PASSWORD -Dedc.vault=$EDC_VAULT -Dedc.fs.config=$EDC_FS_CONFIG -jar /app/pull-connector.jar
