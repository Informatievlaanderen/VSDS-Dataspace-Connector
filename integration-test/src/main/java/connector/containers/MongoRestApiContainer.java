package connector.containers;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MongoRestApiContainer extends GenericContainer {
	private final HttpClient client = HttpClient.newHttpClient();

	public MongoRestApiContainer() {
		super("ghcr.io/informatievlaanderen/mongodb-rest-api");
		addExposedPort(80);
		addEnv("SILENT", "false");
	}

	public MongoRestApiContainer withMongo(String mongoHost) {
		addEnv("CONNECTION_URI", "mongodb://%s:27017".formatted(mongoHost));
		return this;
	}

	public int getMemberCollectionCount() throws InterruptedException, IOException, URISyntaxException {
		URI targetURI = new URI("http://localhost:%d/test/ingest_ldesmember".formatted(this.getMappedPort(80)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.GET()
				.build();

		var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
		JsonReader reader = Json.createReader(new StringReader(response));

		return reader.readObject().getInt("count");
	}


}
