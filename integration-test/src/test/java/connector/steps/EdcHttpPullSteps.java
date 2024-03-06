package connector.steps;

import com.github.dockerjava.api.command.CreateContainerCmd;
import connector.containers.HttpPullContainer;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static connector.steps.PrerequisiteSteps.DID_SERVER;
import static connector.steps.PrerequisiteSteps.network;

public class EdcHttpPullSteps {
	public static Map<String, HttpPullContainer> connectors = new HashMap<>();

	@Given("I have a Provider Connector instance {string}")
	public void iHaveAProviderConnectorInstance(String name) {
		var provider = (HttpPullContainer) new HttpPullContainer()
				.withName(name)
				.withDidServer(DID_SERVER)
				.asProvider()
				.withEnv(Map.of())
				.withClasspathResourceMapping(name, "/tmp", BindMode.READ_WRITE)
				.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd -> cmd.withName(name))
				.withNetwork(network)
				.waitingFor(Wait.forHealthcheck());

		provider.start();

		connectors.put(name, provider);
	}

	@And("I have a Consumer Connector instance {string}")
	public void iHaveAConsumerConnectorInstance(String name) {
		var consumer = (HttpPullContainer) new HttpPullContainer()
				.withName(name)
				.withDidServer(DID_SERVER)
				.withEnv(Map.of())
				.withClasspathResourceMapping(name, "/tmp", BindMode.READ_WRITE)
				.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd -> cmd.withName(name))
				.withNetwork(network)
				.waitingFor(Wait.forHealthcheck());

		consumer.start();

		connectors.put(name, consumer);
	}
}
