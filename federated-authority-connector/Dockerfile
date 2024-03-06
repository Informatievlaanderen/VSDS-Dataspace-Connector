# Build the jar file
FROM openjdk:21-ea-bullseye AS build

WORKDIR /app
COPY ./ .
RUN ./gradlew clean build

# Run the jar file
FROM openjdk:21-ea-bullseye
WORKDIR /app
COPY --from=build /app/federated-authority-connector/build/libs/federated-authority-connector.jar .

# Specify the command to run your application
CMD java -Dedc.fs.config=$EDC_FS_CONFIG -Dfcc.directory.file=$FCC_DIRECTORY_FILE -jar /app/federated-authority-connector.jar
