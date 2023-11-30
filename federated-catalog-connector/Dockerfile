# Build the jar file
FROM openjdk:18-ea-bullseye AS build

WORKDIR /app
COPY ./ .
RUN ./gradlew clean build

# Run the jar file
FROM openjdk:18-ea-bullseye
WORKDIR /app
COPY --from=build /app/federated-catalog-connector/build/libs/federated-catalog-connector.jar .

# Specify the command to run your application
CMD java -Dedc.fs.config=$EDC_FS_CONFIG -Dfcc.directory.file=$FCC_DIRECTORY_FILE -jar /app/federated-catalog-connector.jar
