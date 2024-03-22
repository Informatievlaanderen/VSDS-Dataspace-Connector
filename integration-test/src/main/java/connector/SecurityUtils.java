package connector;

import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import foundation.identity.did.VerificationMethod;
import io.fusionauth.jwks.domain.JSONWebKey;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class SecurityUtils {
	public static final String PASSWORD = "123456";
	public PrivateKey privateKey;
	public X509Certificate certificate;
	public KeyStore keyStore;

	public void generateECKey(String alias) throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateEncodingException, SignatureException, InvalidKeyException, UnrecoverableKeyException {
		String baseDir = "build/resources/test/";
		char[] password = "123456".toCharArray();

		keyStore = getOrGenerateKeyStore(baseDir, alias, "keystore", password);

		privateKey = (PrivateKey) keyStore.getKey(alias, password);

		certificate = (X509Certificate) keyStore.getCertificate(alias);

		createRootDirectory(baseDir + alias);
		writePemFile(privateKey, "PRIVATE KEY", baseDir + alias + "/private.pem");
		writePemFile(certificate.getPublicKey(), "PUBLIC KEY", baseDir + alias + "/public.pem");
		writePemFile(certificate, baseDir + alias + "/cert.pem");
		writeVaultProperties(privateKey, certificate, baseDir + alias + "/vault.properties");
	}

	public void storeDidDocument(String didUrl, String serviceEndpoint, String name) throws IOException {
		String baseDir = "build/resources/test/webdid/";
		URI did = URI.create(didUrl);

		Service service = Service.builder()
				.id(URI.create(did + "#identity-hub-url"))
				.type("IdentityHub")
				.serviceEndpoint(serviceEndpoint)
				.build();

		VerificationMethod verificationMethod = VerificationMethod.builder()
				.controller(URI.create(""))
				.id(URI.create(did + "#" + name))
				.type("JsonWebKey2020")
				.publicKeyJwk(getJwk())
				.build();

		DIDDocument diddoc = DIDDocument.builder()
				.id(did)
				.service(service)
				.verificationMethod(verificationMethod)
				.build();

		createRootDirectory(baseDir + name);
		JsonObject object = Json.createReader(new StringReader(diddoc.toJson())).readObject();

		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		object.forEach(jsonBuilder::add);
		jsonBuilder.add("authentication", Json.createArrayBuilder().add(did + "#" + name).build());

		Files.writeString(Path.of(baseDir + name + "/did.json"), prettyFormatJson(jsonBuilder.build()));
	}

	public void storeDidDocumentForAuthority(String didUrl, String ihServiceEndpoint, String rsServiceEndpoint, String name) throws IOException {
		String baseDir = "build/resources/test/webdid/";
		URI did = URI.create(didUrl);

		Service identityHub = Service.builder()
				.id(URI.create(did + "#registration-url"))
				.type("RegistrationUrl")
				.serviceEndpoint(ihServiceEndpoint)
				.build();

		Service registration = Service.builder()
				.id(URI.create(did + "#identity-hub-url"))
				.type("IdentityHub")
				.serviceEndpoint(rsServiceEndpoint)
				.build();

		VerificationMethod verificationMethod = VerificationMethod.builder()
				.controller(URI.create(""))
				.id(URI.create(did + "#" + name))
				.type("JsonWebKey2020")
				.publicKeyJwk(getJwk())
				.build();

		DIDDocument diddoc = DIDDocument.builder()
				.id(did)
				.services(List.of(identityHub, registration))
				.verificationMethod(verificationMethod)
				.build();

		createRootDirectory(baseDir + name);
		JsonObject object = Json.createReader(new StringReader(diddoc.toJson())).readObject();

		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		object.forEach(jsonBuilder::add);
		jsonBuilder.add("authentication", Json.createArrayBuilder().add(did + "#" + name).build());

		Files.writeString(Path.of(baseDir + name + "/did.json"), prettyFormatJson(jsonBuilder.build()));
	}

	public Map<String, Object> getJwk() {
		Map<String, Object> jwk = new HashMap<>();
		PublicKey publicKey = certificate.getPublicKey();
		JSONWebKey jsonWebKey = JSONWebKey.build(publicKey);
		JsonReader reader = Json.createReader(new StringReader(jsonWebKey.toString()));
		reader.readObject().forEach((string, jsonValue) -> jwk.put(string, ((JsonString) jsonValue).getString()));
		return jwk;
	}

	private KeyStore getOrGenerateKeyStore(String keyStoreFolderPath, String secretEntryAliasName,
	                                       String keyStoreName, char[] password) {
		File file = new File("%s%s/%s.pfx".formatted(keyStoreFolderPath, secretEntryAliasName, keyStoreName));

		if (file.exists()) {
			try {
				return KeyStore.getInstance(file, password);
			} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				KeyPair keyPair = SecurityUtils.generateECKeyPair();
				certificate = createCert(keyPair);

				return generateKeyStore(file, keyPair.getPrivate(), certificate, secretEntryAliasName, password, password);
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException |
			         SignatureException | InvalidKeyException | CertificateException | KeyStoreException | IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static void createRootDirectory(String directory) throws IOException {
		File dir = new File(directory);
		if (!dir.exists()) {
			boolean created = dir.mkdirs();
			if (!created) {
				throw new IOException("Failed to create directories for: " + directory);
			}
		}
	}

	private String prettyFormatJson(JsonObject jsonObject) throws IOException {
		try (StringWriter stringWriter = new StringWriter()) {
			JsonWriter jsonWriter = Json.createWriterFactory(
							Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true))
					.createWriter(stringWriter);
			jsonWriter.writeObject(jsonObject);
			jsonWriter.close();
			return stringWriter.toString();
		}
	}

	private static void writePemFile(Key key, String description, String filename)
			throws IOException {
		try (PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)))) {
			pemWriter.writeObject(new PemObject(description, key.getEncoded()));
		}
	}

	private static void writePemFile(Certificate certificate, String filename)
			throws IOException {
		try (PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)))) {
			pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
		} catch (CertificateEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeVaultProperties(PrivateKey priv, Certificate certificate, String filename)
			throws IOException {

		String content;

		try (var pem = new StringWriter()) {
			try (PemWriter pemWriter = new PemWriter(pem)) {
				pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
			} catch (CertificateEncodingException e) {
				throw new RuntimeException(e);
			}

			content = "public-key=" + pem.toString()
					.replace("\n", "\\n")
					.replace("\r", "\\r") + "\n";
		}
		try (var pem = new StringWriter()) {
			try (PemWriter pemWriter = new PemWriter(pem)) {
				pemWriter.writeObject(new PemObject("PRIVATE KEY", priv.getEncoded()));
			}

			content += "private-key=" + pem.toString()
					.replace("\n", "\\n")
					.replace("\r", "\\r") + "\n";
		}
		Files.writeString(Path.of(filename), content);
	}

	private static KeyPair generateECKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		Security.addProvider(new BouncyCastleProvider());

		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
		generator.initialize(256); // NIST P-256 curve
		return generator.generateKeyPair();
	}

	/**
	 * This method stores the keystore into the file system
	 *
	 * @param key                  - the key itself
	 * @param secretEntryAliasName - alias name for the key
	 * @param secretPassword       - password for the key
	 * @param keyStorePassword     - password to be used for accessing the key store
	 * @return
	 */
	private static KeyStore generateKeyStore(File keystore,
	                                         PrivateKey key, X509Certificate x509Certificate,
	                                         String secretEntryAliasName, char[] secretPassword,
	                                         char[] keyStorePassword) throws KeyStoreException,
			CertificateException, IOException, NoSuchAlgorithmException {

		// getting the algorithm
		KeyStore keyStore = KeyStore.getInstance("PKCS12");  //JCEKS PKCS12

		// initializing the empty stream for new keystore / existing keystore would need inputStream
		keyStore.load(null, keyStorePassword);

		// the protection param is used to protect the secret entry
		KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(secretPassword);


		KeyStore.Entry privateKeyEntry = new KeyStore.PrivateKeyEntry(key, new X509Certificate[]{x509Certificate});

		// adding the entry to the keystore
		keyStore.setEntry(secretEntryAliasName, privateKeyEntry, protectionParam); //"secretKeyAlias"

		//Storing the KeyStore object
		java.io.FileOutputStream fos = new java.io.FileOutputStream(keystore);
		keyStore.store(fos, keyStorePassword);

		return keyStore;
	}

	private static X509Certificate createCert(KeyPair keyPair) throws CertificateEncodingException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
		// build a certificate generator
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal dnName = new X500Principal("cn=Example_CN");

		// add some options
		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setSubjectDN(new X509Name("dc=Example_Name"));
		certGen.setIssuerDN(dnName); // use the same
		// yesterday
		certGen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
		// in 2 years
		certGen.setNotAfter(new Date(System.currentTimeMillis() + 2L * 365 * 24 * 60 * 60 * 1000));
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm("SHA256withECDSA");
		certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping));

		// finally, sign the certificate with the private key of the same KeyPair
		return certGen.generate(keyPair.getPrivate(), "BC");
	}
}
