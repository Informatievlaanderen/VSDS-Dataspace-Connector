package connector.steps;

import com.github.dockerjava.api.command.CreateContainerCmd;
import connector.ServerSeeder;
import connector.containers.LdesServerContainer;
import connector.containers.LdioContainer;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static connector.steps.EdcHttpPullSteps.connectors;
import static connector.steps.PrerequisiteSteps.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LdesSteps {
	public static LdesServerContainer ldesServer;
	public static LdioContainer ldio;
	public static ServerSeeder seeder;
	@Given("I have an LDES Server set up")
	public void iHaveAnLDESServer() throws IOException {
		ldesServer = (LdesServerContainer) new LdesServerContainer("ldes/ldes-server")
				.withDefaultMongoConfiguration(mongo)
				.waitingFor(Wait.forHealthcheck())
				.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd -> cmd.withName("server"))
				.withNetwork(network);

		ldesServer.start();

		// Setup & Seed
		String deviceConfig = Files.readString(Path.of("src/test/resources/devices.ttl"));
		ldesServer.createStream(deviceConfig);

		String devicesPagedConfig = Files.readString(Path.of("src/test/resources/devices.paged.ttl"));
		ldesServer.createView("devices", devicesPagedConfig);

		seeder = new ServerSeeder(ldesServer);
	}

	@When("I ingest {int} members in the LDES Server")
	public void iIngestMembersInTheLDESServer(int count) throws IOException, URISyntaxException, InterruptedException {
		seeder.sendData("devices", count);
		assertEquals(count, mongoRestApi.getMemberCollectionCount());
	}

	@And("I have an LDIO pipeline setup to follow {string}")
	public void iHaveAnLDIOPipelineSetupToFollow(String participant) throws URISyntaxException, IOException, InterruptedException {
		ldio = (LdioContainer) new LdioContainer()
				.waitingFor(Wait.forHealthcheck())
				.withNetwork(network)
				.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd -> cmd.withName("ldio-client"));

		ldio.start();

		ldio.addLdesClientConnectorPipeline(connectors.get(participant));
	}

	@And("the LDIO has started")
	public void theLDIOHasStarted() {
		Awaitility.await()
				.until(() -> ldio.getLogs().contains("Parsing response for:"));
	}
}
