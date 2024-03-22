package connector;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class DataSpaceInteractor {
	public static String dataPlaneCreationRequest(String providerUrl, int publicPort, int controlPort) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("@context", Json.createObjectBuilder()
				.add("edc", "https://w3id.org/edc/v0.0.1/ns/"));
		jsonBuilder.add("url", "%s:%s/control/transfer".formatted(providerUrl, controlPort));
		jsonBuilder.add("allowedSourceTypes", Json.createArrayBuilder()
				.add("HttpData"));
		jsonBuilder.add("allowedDestTypes", Json.createArrayBuilder()
				.add("HttpProxy")
				.add("HttpData"));
		jsonBuilder.add("properties", Json.createObjectBuilder()
				.add("https://w3id.org/edc/v0.0.1/ns/publicApiUrl", "%s:%s/public/".formatted(providerUrl, publicPort)));


		return jsonBuilder.build().toString();
	}

	public static String assetCreationRequest(String assetName, String ldesServerStreamUrl) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("@context", Json.createObjectBuilder()
				.add("edc", "https://w3id.org/edc/v0.0.1/ns/"));
		jsonBuilder.add("@id", assetName);
		jsonBuilder.add("properties", Json.createObjectBuilder()
				.add("name", assetName)
				.add("contenttype", "application/n-quads"));
		jsonBuilder.add("dataAddress", Json.createObjectBuilder()
				.add("name", assetName)
				.add("type", "HttpData")
				.add("baseUrl", ldesServerStreamUrl)
				.add("proxyPath", "true")
				.add("proxyQueryParams", "true")
				.add("header:Accept", "application/n-quads")
				.add("contenttype", "application/n-quads"));

		return jsonBuilder.build().toString();
	}

	public static String policyCreationRequest(String policyId) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("@context", Json.createObjectBuilder()
				.add("edc", "https://w3id.org/edc/v0.0.1/ns/")
				.add("odrl", "http://www.w3.org/ns/odrl/2/"));
		jsonBuilder.add("@id", policyId);
		jsonBuilder.add("policy", Json.createObjectBuilder()
				.add("@type", "set")
				.add("odrl:permission", Json.createArrayBuilder())
				.add("odrl:prohibition", Json.createArrayBuilder())
				.add("odrl:obligation", Json.createArrayBuilder()));

		return jsonBuilder.build().toString();
	}

	public static String contractCreationRequest(String contractId, String accessPolicyId,
	                                             String contractPolicyId) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("@context", Json.createObjectBuilder()
				.add("edc", "https://w3id.org/edc/v0.0.1/ns/"));
		jsonBuilder.add("@id", contractId);
		jsonBuilder.add("accessPolicyId", accessPolicyId);
		jsonBuilder.add("contractPolicyId", contractPolicyId);
		jsonBuilder.add("assetsSelector", Json.createArrayBuilder());

		return jsonBuilder.build().toString();
	}

	public static String contractNegotiationRequest(String providerAddress, String providerId,
	                                                JsonObject policy, String assetId) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("@context", Json.createObjectBuilder()
				.add("edc", "https://w3id.org/edc/v0.0.1/ns/")
				.add("odrl", "http://www.w3.org/ns/odrl/2/"));
		jsonBuilder.add("@type", "NegotiationInitiateRequestDto");
		jsonBuilder.add("connectorAddress", providerAddress);
		jsonBuilder.add("providerId", providerId);
		jsonBuilder.add("protocol", "dataspace-protocol-http");
		jsonBuilder.add("offer", Json.createObjectBuilder()
				.add("offerId", policy.getString("@id"))
				.add("assetId", assetId)
				.add("policy", policy));

		return jsonBuilder.build().toString();
	}

	public static String transferRequest(String providerDid, String providerUrl, String contractId, String assetId, String ldioTokenEndpoint) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("@context", Json.createObjectBuilder()
				.add("@vocab", "https://w3id.org/edc/v0.0.1/ns/"));
		jsonBuilder.add("@type", "TransferRequest");
		jsonBuilder.add("connectorId", providerDid);
		jsonBuilder.add("connectorAddress", providerUrl);
		jsonBuilder.add("contractId", contractId);
		jsonBuilder.add("assetId", assetId);
		jsonBuilder.add("protocol", "dataspace-protocol-http");
		jsonBuilder.add("dataDestination", Json.createObjectBuilder()
				.add("@type", "DataAddress")
				.add("type", "HttpProxy"));
		jsonBuilder.add("privateProperties", Json.createObjectBuilder()
				.add("receiverHttpEndpoint", ldioTokenEndpoint));

		return jsonBuilder.build().toString();
	}
}
