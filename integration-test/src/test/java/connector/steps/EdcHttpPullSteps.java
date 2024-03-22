package connector.steps;

import com.github.dockerjava.api.command.CreateContainerCmd;
import connector.containers.HttpPullContainer;
import connector.containers.LdesServerContainer;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.json.JsonObject;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static connector.steps.LdesSteps.ldesServer;
import static connector.steps.LdesSteps.ldio;
import static connector.steps.PrerequisiteSteps.DID_SERVER;
import static connector.steps.PrerequisiteSteps.network;

public class EdcHttpPullSteps {
	public static Map<String, HttpPullContainer> connectors = new HashMap<>();
	private JsonObject activePolicy;
	private String activeNegotiationId;

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

	@When("Participant {string} gets the policyId from {string}")
	public void participantGetsThePolicyIdFromThe(String consumer, String provider) throws URISyntaxException, IOException, InterruptedException {
		activePolicy = connectors.get(consumer)
				.getPolicyFromProvider(connectors.get(provider));
	}

	@When("I create a HTTP dataplane in {string}")
	public void iCreateAHTTPDataplaneIn(String participant) throws URISyntaxException, IOException, InterruptedException {
		connectors.get(participant).createDataplane();
	}

	@And("I create an asset {string} of the LDES Server in {string}")
	public void iCreateAnAssetOfTheLDESServerIn(String assetName, String participant) throws URISyntaxException, IOException, InterruptedException {
		String ldesUrl = "http:/%s:%s/%s".formatted(ldesServer.getContainerName(), LdesServerContainer.DEFAULT_PORT, assetName);
		connectors.get(participant).createAsset(assetName, ldesUrl);
	}

	@And("I create a blank-cheque policy {string} in {string}")
	public void iCreateABlankChequePolicyIn(String policyId, String participant) throws URISyntaxException, IOException, InterruptedException {
		connectors.get(participant).createPolicy(policyId);
	}

	@And("I create a contract {string} with policy {string} and access policy {string} for all assets in {string}")
	public void iCreateAContractWithPolicyAndAccessPolicyForAllAssetsIn(String contractId, String contractPolicyId,
	                                                                    String accesPolicyId, String participant) throws URISyntaxException, IOException, InterruptedException {
		connectors.get(participant).createContract(contractId, accesPolicyId, contractPolicyId);
	}

	@And("Participant {string} starts negotiating a contract with {string} for asset {string}")
	public void participantStartsNegotiatingAContractWithForAsset(String participant, String provider, String assetId) throws URISyntaxException, IOException, InterruptedException {
		activeNegotiationId = connectors.get(participant)
				.negotiateContract(assetId, activePolicy, connectors.get(provider));
	}

	@Then("I wait for {string}'s contract negotiation to finish")
	public void iWaitForSContractNegotiationToFinish(String participant) throws IOException, URISyntaxException, InterruptedException {
		connectors.get(participant).checkForNegotiationCompletion(activeNegotiationId);

		Awaitility.await()
				.atMost(1, TimeUnit.MINUTES)
				.until(() -> connectors.get(participant).checkForNegotiationCompletion(activeNegotiationId));
	}

	@And("Participant {string} starts the transfer of {string} in {string}")
	public void participantStartsTheTransferOfIn(String participant, String asset, String provider) throws URISyntaxException, IOException, InterruptedException {
		connectors.get(participant).startTransfer(activeNegotiationId, ldio, asset, connectors.get(provider));
	}
}
