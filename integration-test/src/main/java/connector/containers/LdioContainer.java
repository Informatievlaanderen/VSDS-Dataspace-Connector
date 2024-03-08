package connector.containers;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

public class LdioContainer extends GenericContainer {
	private final HttpClient client = HttpClient.newHttpClient();
	public LdioContainer() {
		super("ghcr.io/informatievlaanderen/ldi-orchestrator:20240308141921");
		addExposedPorts(8082);
		addEnv("server.port", "8082");
		addEnv("orchestrator.pipelines", "");
		addEnv("management.endpoints.web.exposure.include[0]", "prometheus");
	}

	@Override
	public boolean isHealthy() {
		return this.getLogs().contains("Started Application in");
	}

	public void addLdesClientConnectorPipeline(HttpPullContainer provider) throws URISyntaxException, IOException, InterruptedException {
		var pipeline = ldesClientConnectorPipeline(provider.getContainerName());

		var targetURI = new URI("http://localhost:%s/admin/api/v1/pipeline".formatted(this.getMappedPort(8082)));

		var httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(pipeline))
				.build();

		var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());


	}

	private String ldesClientConnectorPipeline(String consumer) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("name", "client-pipeline");
		jsonBuilder.add("description", "Simple LdioLdesClientConnector in, console out pipeline.");
		jsonBuilder.add("input", Json.createObjectBuilder()
				.add("name", "Ldio:LdesClientConnector")
				.add("config", Json.createObjectBuilder()
						.add("urls", "http:/%s:9291/public".formatted(consumer))
						.add("connector-transfer-url", "http:/%s:9193/management/v2/transferprocesses".formatted(consumer))
						.add("proxy-url-to-replace", "http://localhost:8081/devices")
						.add("proxy-url-replacement", "http://%s:9291/public".formatted(consumer))
						.add("source-format", "application/n-quads")));
		jsonBuilder.add("outputs", Json.createArrayBuilder()
				.add(Json.createObjectBuilder()
						.add("name", "Ldio:ConsoleOut")));

		return jsonBuilder.build().toString();
	}
}
