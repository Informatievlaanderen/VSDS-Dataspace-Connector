package connector.containers;

import com.github.dockerjava.api.command.CreateContainerCmd;
import connector.DataPlaneInteractor;
import connector.SecurityUtils;
import connector.security.JwtGenerator;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static connector.DataPlaneInteractor.dataPlaneCreationRequest;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public class HttpPullContainer extends GenericContainer {
	private final HttpClient client = HttpClient.newHttpClient();
	private final Map<String, String> portMapping = ofEntries(
			entry("web.http.port", "9191"),
			entry("web.http.path", "/api"),
			entry("web.http.management.port", "9193"),
			entry("web.http.management.path", "/management"),
			entry("web.http.protocol.port", "9194"),
			entry("web.http.protocol.path", "/protocol"),
			entry("web.http.authority.port", "8180"),
			entry("web.http.authority.path", "/authority"),
			entry("web.http.public.port", "9291"),
			entry("web.http.public.path", "/public"),
			entry("web.http.control.port", "9192"),
			entry("web.http.control.path", "/control"));

	private Map<String, String> authConfig;

	private Map<String, String> corsConfig = new HashMap<>();

	private Map<String, String> didConfig;
	private String name;
	private String didUrl;
	private SecurityUtils securityUtils = new SecurityUtils();

	public HttpPullContainer() {
		super("vsds-dataspace-connector/http-pull:local");
		setExposedPorts(List.of(8180, 9191, 9192, 9193, 9194, 9291));
	}

	public HttpPullContainer withName(String name) {
		this.name = name;
		try {
			securityUtils.generateECKey(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		authConfig = Map.of("edc.public.key.alias", "public-key",
				"edc.transfer.dataplane.token.signer.privatekey.alias", name,
				"edc.transfer.proxy.token.signer.privatekey.alias", name,
				"edc.transfer.proxy.token.verifier.publickey.alias", "public-key");

		return (HttpPullContainer) withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) createContainerCmd ->
				createContainerCmd.withName(name));
	}

	public HttpPullContainer withDidServer(String didServer) {
		this.didUrl = "did:web:%s:%s".formatted(didServer, name);
		String didJwtAudience = "http://%s:8180/authority".formatted(name);

		didConfig = Map.of("edc.participant.id", "did:web:%s:%s".formatted(didServer, name),
				"edc.connector.name", name,
				"edc.identity.did.url", didUrl,
				"jwt.audience", didJwtAudience,
				"edc.iam.did.web.use.https", "false",
				"edc.dataplane.token.validation.endpoint", "http://%s:9192/control/token".formatted(name),
				"edc.dsp.callback.address", "http://%s:9194/protocol".formatted(name),
				"edc.keystore", "/tmp/keystore.pfx",
				"edc.keystore.password", SecurityUtils.PASSWORD,
				"edc.vault", "/tmp/vault.properties");

		return this;
	}

	public HttpPullContainer asProvider() {
		corsConfig = Map.of("edc.web.rest.cors.enabled", "true",
				"edc.web.rest.cors.headers", "'origin,content-type,accept,authorization,x-api-key'",
				"edc.web.rest.cors.origins", "'*'");

		return this;
	}

	@SuppressWarnings("DuplicatedCode")
	@Override
	public HttpPullContainer withEnv(Map config) {
		if (didConfig == null) {
			throw new RuntimeException("Did Config is not set. Call withName method first");
		}

		Map<String, String> combinedConfig = new HashMap<String, String>(config);
		combinedConfig.putAll(portMapping);
		combinedConfig.putAll(didConfig);
		combinedConfig.putAll(authConfig);
		combinedConfig.putAll(corsConfig);

		combinedConfig.forEach(this::addEnv);

		return this;
	}

	@Override
	public boolean isHealthy() {
		return this.getLogs().contains(name + " ready");
	}

	public void registerAsParticipant(FederatedAuthorityContainer authorityContainer) throws URISyntaxException, IOException, InterruptedException {
		var jwt = JwtGenerator.generateJwtToken(didUrl, didUrl, authorityContainer.getJwtAudience(),
				(ECPublicKey) securityUtils.keyPair.getPublic(), (ECPrivateKey) securityUtils.keyPair.getPrivate());

		URI targetURI = new URI("http://localhost:%s/authority/registry/participant".formatted(authorityContainer.getMappedPort(8180)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.POST(HttpRequest.BodyPublishers.noBody())
				.header("Authorization", "Bearer %s".formatted(jwt))
				.build();

		client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
	}

	public void generateDidFile() throws IOException {
		var serviceEndpoint = "http:/%s:%s/api/identity-hub".formatted(getContainerName(), 9191);
		securityUtils.storeDidDocument(didUrl, serviceEndpoint, name);
	}

	public JsonObject getPolicyFromProvider(HttpPullContainer provider) throws URISyntaxException, IOException, InterruptedException {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("@context", Json.createObjectBuilder().add("edc", "https://w3id.org/edc/v0.0.1/ns/").build());
		jsonBuilder.add("providerUrl", "http:/%s:%s/protocol".formatted(provider.getContainerName(), 9194));
		jsonBuilder.add("protocol", "dataspace-protocol-http");

		URI targetURI = new URI("http://localhost:%s/management/v2/catalog/request".formatted(this.getMappedPort(9193)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.POST(HttpRequest.BodyPublishers.ofString(jsonBuilder.build().toString()))
				.header("Content-Type", "application/json")
				.build();

		var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

		JsonObject catalog = Json.createReader(new StringReader(response.body())).readObject();

		return catalog.getJsonObject("dcat:dataset")
				.getJsonObject("odrl:hasPolicy");
	}

	public void createDataplane() throws URISyntaxException, IOException, InterruptedException {
		String dataplaneRequest = dataPlaneCreationRequest("http:/%s".formatted(getContainerName()), 9192);

		URI targetURI = new URI("http://localhost:%s/management/v2/dataplanes".formatted(this.getMappedPort(9193)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.POST(HttpRequest.BodyPublishers.ofString(dataplaneRequest))
				.header("Content-Type", "application/json")
				.build();

		client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
	}

	public void createAsset(String assetName, String formatted) throws URISyntaxException, IOException, InterruptedException {
		var assetCreationRequest = DataPlaneInteractor.assetCreationRequest(assetName, formatted);

		URI targetURI = new URI("http://localhost:%s/management/v3/assets".formatted(this.getMappedPort(9193)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.POST(HttpRequest.BodyPublishers.ofString(assetCreationRequest))
				.header("Content-Type", "application/json")
				.build();

		client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
	}

	public void createPolicy(String policyId) throws URISyntaxException, IOException, InterruptedException {
		var policyCreationRequest = DataPlaneInteractor.policyCreationRequest(policyId);

		URI targetURI = new URI("http://localhost:%s/management/v2/policydefinitions".formatted(this.getMappedPort(9193)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.POST(HttpRequest.BodyPublishers.ofString(policyCreationRequest))
				.header("Content-Type", "application/json")
				.build();

		client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
	}

	public void createContract(String contractId, String accessPolicyId, String contractPolicyId) throws URISyntaxException, IOException, InterruptedException {
		var contractCreationRequest = DataPlaneInteractor.contractCreationRequest(contractId, accessPolicyId, contractPolicyId);

		URI targetURI = new URI("http://localhost:%s/management/v2/contractdefinitions".formatted(this.getMappedPort(9193)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.POST(HttpRequest.BodyPublishers.ofString(contractCreationRequest))
				.header("Content-Type", "application/json")
				.build();

		client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
	}

	public String negotiateContract(String assetId, JsonObject policy, HttpPullContainer provider) throws URISyntaxException, IOException, InterruptedException {
		var providerAddress = "http:/%s:9194/protocol".formatted(provider.getContainerName());
		var providerId = "did:web:did-server:%s".formatted(provider.getContainerName().substring(1));

		var contractNegotiationRequest = DataPlaneInteractor.contractNegotiationRequest(providerAddress, providerId, policy, assetId);

		URI targetURI = new URI("http://localhost:%s/management/v2/contractnegotiations".formatted(this.getMappedPort(9193)));
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.POST(HttpRequest.BodyPublishers.ofString(contractNegotiationRequest))
				.header("Content-Type", "application/json")
				.build();

		var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

		return Json.createReader(new StringReader(response.body()))
				.readObject()
				.getString("@id");
	}

	public boolean checkForNegotiationCompletion(String activeNegotiationId) throws IOException, InterruptedException, URISyntaxException {
		URI targetURI = new URI("http://localhost:%s/management/v2/contractnegotiations/%s".formatted(this.getMappedPort(9193), activeNegotiationId));

		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.GET()
				.build();

		var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

		return response.body().contains("FINALIZED");
	}

	public void startTransfer(String activeNegotiationId, LdioContainer ldio, String asset, HttpPullContainer provider) throws URISyntaxException, IOException, InterruptedException {
		URI targetURI = new URI("http://localhost:%s/management/v2/contractnegotiations/%s".formatted(this.getMappedPort(9193), activeNegotiationId));

		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.GET()
				.build();

		var response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

		var contractAgreementId = Json.createReader(new StringReader(response.body()))
				.readObject()
				.getString("contractAgreementId");

		var providerAddress = "http:/%s:9194/protocol".formatted(provider.getContainerName());
		var ldioTransferEndpoint = "http:/%s:8082/client-pipeline/token".formatted(ldio.getContainerName());
		var transferRequest = DataPlaneInteractor.transferRequest(provider.getDidUrl(), providerAddress, contractAgreementId, asset, ldioTransferEndpoint);

		targetURI = new URI("http://localhost:%s/client-pipeline/transfer".formatted(ldio.getMappedPort(8082)));

		httpRequest = HttpRequest.newBuilder()
				.uri(targetURI)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(transferRequest))
				.build();

		response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

		response.body().contains("FINALIZED");
	}

	public String getDidUrl() {
		return didUrl;
	}
}
