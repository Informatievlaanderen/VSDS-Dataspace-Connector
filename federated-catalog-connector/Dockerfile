# Build the jar file
FROM openjdk:18-ea-bullseye AS build

WORKDIR /app
COPY .. .
RUN ./gradlew clean build -p ./federated-catalog-connector

# Run the jar file
FROM openjdk:18-ea-bullseye
WORKDIR /app
COPY --from=build /app/federated-catalog-connector/build/libs/federated-catalog-connector.jar .

# Specify the command to run your application
CMD java -jar /app/federated-catalog-connector.jar
