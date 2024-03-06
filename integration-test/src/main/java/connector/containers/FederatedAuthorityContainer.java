package connector.containers;

import com.github.dockerjava.api.command.CreateContainerCmd;
import connector.SecurityUtils;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FederatedAuthorityContainer extends GenericContainer {
	private SecurityUtils securityUtils = new SecurityUtils();
	private String name;
	Map<String, String> defaultConfig = Map.of("web.http.authority.port", "8180",
			"web.http.authority.path", "/authority",
			"web.http.port", "8181",
			"web.http.path", "/api",
			"edc.iam.did.web.use.https", "false");

	Map<String, String> crawlerConfig = Map.of("edc.catalog.cache.execution.delay.seconds", "0",
			"edc.catalog.cache.execution.period.seconds", "86400", // One day (high enough to not bother)
			"edc.catalog.cache.partition.num.crawlers", "0",
			"fcc.directory.file", "tmp/nodes-dc.json");

	Map<String, String> didConfig;
	private String jwtAudience;

	public FederatedAuthorityContainer(String dockerImageName) {
		super(dockerImageName);
		addExposedPorts(8180, 8181);
	}

	public FederatedAuthorityContainer withName(String name) {
		this.name = name;
		try {
			securityUtils.generateECKey(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (FederatedAuthorityContainer) withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) createContainerCmd ->
				createContainerCmd.withName(name));
	}

	public FederatedAuthorityContainer withDidServer(String didServer) {
		jwtAudience = "http://%s:8180/authority".formatted(name);
		didConfig = Map.of("edc.connector.name", name,
				"edc.identity.did.url", "did:web:%s:%s".formatted(didServer, name),
				"jwt.audience", jwtAudience,
				"edc.iam.did.web.use.https", "false",
				"edc.keystore", "/tmp/keystore.pfx",
				"edc.keystore.password", SecurityUtils.PASSWORD,
				"edc.vault", "/tmp/vault.properties");

		return this;
	}

	public FederatedAuthorityContainer withScraper(int executionDelay, int executionSeconds,
	                                               int partitionCrawlers, HttpPullContainer... pullContainers) {
		crawlerConfig = Map.of("edc.catalog.cache.execution.delay.seconds", String.valueOf(executionDelay),
				"edc.catalog.cache.execution.period.seconds", String.valueOf(executionSeconds),
				"edc.catalog.cache.partition.num.crawlers", String.valueOf(partitionCrawlers));

		return this;
	}

	@Override
	public FederatedAuthorityContainer withEnv(Map config) {
		if (didConfig == null) {
			throw new RuntimeException("Did Config is not set. Call withName method first");
		}

		Map<String, String> combinedConfig = new HashMap<String, String>(config);
		combinedConfig.putAll(defaultConfig);
		combinedConfig.putAll(didConfig);
		combinedConfig.putAll(crawlerConfig);

		combinedConfig.forEach(this::addEnv);

		return this;
	}

	@Override
	public boolean isHealthy() {
		return this.getLogs().contains(name + " ready");
	}

	public String getJwtAudience() {
		return jwtAudience;
	}
}
