package connector.steps;

import connector.containers.FederatedAuthorityContainer;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.testcontainers.containers.BindMode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static connector.steps.EdcHttpPullSteps.connectors;
import static connector.steps.PrerequisiteSteps.*;

public class EdcFederatedAuthoritySteps {
	public static Map<String, FederatedAuthorityContainer> authorities = new HashMap<>();

	@When("I have a Federated Authority instance {string}")
	public void iHaveAFederatedAuthorityInstance(String name) throws IOException {
		var federatedAuthority = (FederatedAuthorityContainer) new FederatedAuthorityContainer("vsds-dataspace-connector/federated-authority:local")
				.withName(FEDERATED_AUTHORITY)
				.withDidServer(DID_SERVER)
				.withEnv(Map.of())
				.withNetwork(network)
				.withClasspathResourceMapping(FEDERATED_AUTHORITY, "/tmp", BindMode.READ_WRITE);

		federatedAuthority.start();

		federatedAuthority.generateDidFile();

		authorities.put(name, federatedAuthority);
	}

	@When("I register {string} to the Authority {string}")
	public void iRegisterToTheAuthority(String participant, String authority) throws URISyntaxException, IOException, InterruptedException {
		connectors.get(participant).registerAsParticipant(authorities.get(authority));
	}

	@And("Authority contains a did entry for {string}")
	public void authorityContainsADidEntryFor(String participant) throws IOException {
		connectors.get(participant).generateDidFile();
	}
}
