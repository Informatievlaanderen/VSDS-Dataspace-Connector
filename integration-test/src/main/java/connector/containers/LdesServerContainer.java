package connector.containers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class LdesServerContainer extends GenericContainer {
	public static final int DEFAULT_PORT = 8081;
	private final Logger logger = LoggerFactory.getLogger(LdesServerContainer.class);
	private final HttpClient client = HttpClient.newHttpClient();
	public LdesServerContainer(String dockerImageName) {
		super(dockerImageName);
		addFixedExposedPort(DEFAULT_PORT, DEFAULT_PORT);
	}


	public boolean createStream(String content) {
		try {
			URI targetURI = new URI("http://localhost:%s/admin/api/v1/eventstreams".formatted(this.getMappedPort(DEFAULT_PORT)));
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(targetURI)
					.POST(HttpRequest.BodyPublishers.ofString(content))
					.header("Content-Type", "text/turtle")
					.build();

			client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			return true;
		}
		catch (InterruptedException | IOException | URISyntaxException e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	public boolean createView(String eventStream, String content) {
		try {
			URI targetURI = new URI("http://localhost:%s/admin/api/v1/eventstreams/%s/views".formatted(this.getMappedPort(DEFAULT_PORT), eventStream));
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(targetURI)
					.POST(HttpRequest.BodyPublishers.ofString(content))
					.header("Content-Type", "text/turtle")
					.build();

			client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			return true;
		}
		catch (InterruptedException | IOException | URISyntaxException e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	public void postMember(String eventStream, String content) {
		try {
			URI targetURI = new URI("http://localhost:%s/%s".formatted(this.getMappedPort(DEFAULT_PORT), eventStream));
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(targetURI)
					.POST(HttpRequest.BodyPublishers.ofString(content))
					.header("Content-Type", "text/turtle")
					.build();

			client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		}
		catch (InterruptedException | IOException | URISyntaxException e) {
			logger.error(e.getMessage());
		}
	}



	@Override
	public boolean isHealthy() {
		return this.getLogs().contains("Started Application in") && !this.getLogs().contains("ERROR");
	}

	public LdesServerContainer withDefaultMongoConfiguration(MongoDBContainer mongo) {
		return (LdesServerContainer) this.dependsOn(mongo)
				.withEnv(Map.of("server.port", String.valueOf(LdesServerContainer.DEFAULT_PORT),
						"spring.data.mongodb.host", mongo.getContainerName().replace("/", ""),
						"springdoc.swagger-ui.path", "/v1/swagger",
						"management.tracing.enabled", "false",
						"ldes-server.host-name", "http://localhost:8081"));
	}
}
