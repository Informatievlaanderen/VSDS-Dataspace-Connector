package connector.steps;

import connector.ServerSeeder;
import connector.containers.LdesServerContainer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static connector.steps.PrerequisiteSteps.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LdesServerSteps {
	LdesServerContainer ldesServer;
	ServerSeeder seeder;
	@Given("I have an LDES Server set up")
	public void iHaveAnLDESServer() throws IOException {
		ldesServer = (LdesServerContainer) new LdesServerContainer("ldes/ldes-server")
				.withDefaultMongoConfiguration(mongo)
				.waitingFor(Wait.forHealthcheck())
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
}
