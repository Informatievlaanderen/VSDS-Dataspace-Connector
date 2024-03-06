package connector;

import connector.containers.FederatedAuthorityContainer;
import connector.containers.HttpPullContainer;
import connector.containers.LdesServerContainer;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;
import java.util.stream.Stream;

import static connector.steps.PrerequisiteSteps.network;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.junit.Assert.assertEquals;

@Testcontainers
class SandboxApplicationTests {
	private static final String DID_SERVER = "did-server";
	private static final String FEDERATED_AUTHORITY = "federated-authority";
	private static final String CONSUMER = "consumer";
	private static final String PROVIDER = "provider";

	private final HttpClient client = HttpClient.newHttpClient();

	private final String webDidPath = "build/resources/test/webdid";

	@Container
	public MongoDBContainer mongo = new MongoDBContainer("mongo:latest")
			.waitingFor(Wait.forLogMessage(".*Waiting for connections.*", 1))
			.withNetwork(network);

	@Container
	public NginxContainer<?> webdid = new NginxContainer<>("nginx")
			.withClasspathResourceMapping("webdid", "/usr/share/nginx/html", BindMode.READ_WRITE)
			.withCreateContainerCmdModifier(cmd -> cmd.withName(DID_SERVER))
			.waitingFor(new HttpWaitStrategy());

	public LdesServerContainer ldesServer;
	public FederatedAuthorityContainer federatedAuthority;

	public HttpPullContainer consumer;
	public HttpPullContainer provider;

	@BeforeEach
	void setUp() {

		// Start LDES Server
		ldesServer = (LdesServerContainer) new LdesServerContainer("ldes/ldes-server")
				.withDefaultMongoConfiguration(mongo)
				.waitingFor(Wait.forHealthcheck())
				.withNetwork(network);

		federatedAuthority = (FederatedAuthorityContainer) new FederatedAuthorityContainer("vsds-dataspace-connector/federated-authority:local")
				.withName(FEDERATED_AUTHORITY)
				.withDidServer(DID_SERVER)
				.withEnv(Map.of())
				.withClasspathResourceMapping(FEDERATED_AUTHORITY, "/tmp", BindMode.READ_WRITE);

		consumer = (HttpPullContainer) new HttpPullContainer()
				.withName(CONSUMER)
				.withDidServer(DID_SERVER)
				.withEnv(Map.of())
				.withClasspathResourceMapping(CONSUMER, "/tmp", BindMode.READ_WRITE)
				.waitingFor(Wait.forHealthcheck());

		provider = (HttpPullContainer) new HttpPullContainer()
				.withName(PROVIDER)
				.withDidServer(DID_SERVER)
				.asProvider()
				.withEnv(Map.of())
				.withClasspathResourceMapping(PROVIDER, "/tmp", BindMode.READ_WRITE)
				.waitingFor(Wait.forHealthcheck());

		Stream.of(federatedAuthority, consumer, provider, ldesServer)
				.parallel()
				.forEach(GenericContainer::start);

//		consumer.registerAsParticipant(authorities.get(authority));
	}

	@AfterEach
	void cleanup() {
		Stream.of(federatedAuthority, consumer, provider, ldesServer).parallel().forEach(GenericContainer::stop);
	}

	@Test
	public void givenSimpleWebServerContainer_whenGetReuqest_thenReturnsResponse() throws URISyntaxException, IOException, InterruptedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
		setupAndSeedServer();

		var memberCount = getLdesServerFragmentMemberCount();

		assertEquals(50, memberCount);

		// Create a temporary dir
		File contentFolder = new File(webDidPath);

		File newFile = new File(contentFolder, "test.html");
		newFile.deleteOnExit();
		Files.write(newFile.toPath(), "df".getBytes());

		assertEquals(50, memberCount);
	}

	private int getLdesServerFragmentMemberCount() throws IOException, InterruptedException, URISyntaxException {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(new URI("http://%s:%s/devices/paged?pageNumber=1".formatted(ldesServer.getHost(), LdesServerContainer.DEFAULT_PORT)))
				.build();

		var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

		return RDFParser.fromString(response)
				.lang(Lang.TURTLE)
				.toModel()
				.listObjectsOfProperty(createProperty("https://w3id.org/tree#member"))
				.toList()
				.size();
	}

	private void setupAndSeedServer() throws IOException {
		// Setup & Seed
		String deviceConfig = Files.readString(Path.of("src/test/resources/devices.ttl"));
		ldesServer.createStream(deviceConfig);

		String devicesPagedConfig = Files.readString(Path.of("src/test/resources/devices.paged.ttl"));
		ldesServer.createView("devices", devicesPagedConfig);

		ServerSeeder seeder = new ServerSeeder(ldesServer);
		seeder.sendData("devices", 50);
	}


}
