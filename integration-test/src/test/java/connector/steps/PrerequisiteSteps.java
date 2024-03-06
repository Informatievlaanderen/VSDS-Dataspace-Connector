package connector.steps;

import connector.containers.MongoRestApiContainer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.util.stream.Stream;

public class PrerequisiteSteps {
	public static final String MONGO = "mongo";
	public static final String DID_SERVER = "did-server";
	public static final String FEDERATED_AUTHORITY = "federated-authority";
	public static final String CONSUMER = "consumer";
	public static final String PROVIDER = "provider";

	public static final Network network = Network.newNetwork();

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:latest")
			.waitingFor(Wait.forLogMessage(".*Waiting for connections.*", 1))
			.withCreateContainerCmdModifier(cmd -> cmd.withName(MONGO))
			.withNetwork(network);

	@Container
	public static final MongoRestApiContainer mongoRestApi = (MongoRestApiContainer) new MongoRestApiContainer()
			.withMongo(MONGO)
			.dependsOn(mongo)
			.withNetwork(network);


	@Container
	public static final NginxContainer<?> webdid = new NginxContainer<>("nginx")
			.withClasspathResourceMapping("webdid", "/usr/share/nginx/html", BindMode.READ_WRITE)
			.withCreateContainerCmdModifier(cmd -> cmd.withName(DID_SERVER))
			.withNetwork(network)
			.waitingFor(new HttpWaitStrategy());

	@Before
	public void setUp() throws Exception {
		Stream.of(mongo, mongoRestApi, webdid).forEach(GenericContainer::start);
	}

	@After
	public void tearDown() throws Exception {
		EdcHttpPullSteps.connectors.forEach((string, httpPullContainer) -> httpPullContainer.stop());
		EdcFederatedAuthoritySteps.authorities.forEach((string, authority) -> authority.stop());
		mongo.stop();
		mongoRestApi.stop();
		webdid.stop();
	}
}
