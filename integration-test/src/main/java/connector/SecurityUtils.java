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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SecurityUtils {
	public static final String PASSWORD = "123456";
	public KeyPair keyPair;
	public X509Certificate certificate;
	public KeyStore keyStore;

	public void generateECKey(String alias) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, KeyStoreException, CertificateEncodingException, SignatureException, InvalidKeyException {
		String baseDir = "build/resources/test/";

		keyPair = SecurityUtils.generateECKeyPair();
		PrivateKey priv = keyPair.getPrivate();
		PublicKey pub = keyPair.getPublic();

		certificate = createCert(keyPair);

		createRootDirectory(baseDir + alias);
		writePemFile(priv, "PRIVATE KEY", baseDir + alias + "/private.pem");
		writePemFile(pub, "PUBLIC KEY", baseDir + alias + "/public.pem");
		writePemFile(certificate, baseDir + alias + "/cert.pem");
		writeVaultProperties(priv, certificate, baseDir + alias + "/vault.properties");

		try {
			keyStore = storeSecretKeyInKeyStore(priv, certificate, alias, "123456", baseDir, "keystore", "123456");
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
	}

	public void storeDidDocument(String didUrl, Integer identityHubPort, String name) throws IOException {
		String baseDir = "build/resources/test/webdid/";
		URI did = URI.create(didUrl);

		Service service = Service.builder()
				.id(URI.create(did + "#identity-hub-url"))
				.type("IdentityHub")
				.serviceEndpoint("http://localhost:%s/api/identity-hub".formatted(identityHubPort))
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

	public Map<String, Object> getJwk() {
		Map<String, Object> jwk = new HashMap<>();
		PublicKey publicKey = keyPair.getPublic();
		JSONWebKey jsonWebKey = JSONWebKey.build(publicKey);
		JsonReader reader = Json.createReader(new StringReader(jsonWebKey.toString()));
		reader.readObject().forEach((string, jsonValue) -> jwk.put(string, ((JsonString) jsonValue).getString()));
		return jwk;
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
	 * @param keyStoreFolderPath   - file system path to store the keystore
	 * @param keyStoreName         - filename of the keystore file to be saved
	 * @param keyStorePassword     - password to be used for accessing the key store
	 * @return
	 */
	private static KeyStore storeSecretKeyInKeyStore(PrivateKey key, X509Certificate x509Certificate,
	                                                 String secretEntryAliasName, String secretPassword,
	                                                 String keyStoreFolderPath, String keyStoreName,
	                                                 String keyStorePassword) throws KeyStoreException,
			CertificateException, IOException, NoSuchAlgorithmException {

		// getting the algorithm
		KeyStore keyStore = KeyStore.getInstance("PKCS12");  //JCEKS PKCS12

		char[] keyStorePassCharArray = keyStorePassword.toCharArray(); // changeit
		char[] secretEntryPassCharArray = secretPassword.toCharArray();

		// initializing the empty stream for new keystore / existing keystore would need inputStream
		keyStore.load(null, keyStorePassCharArray);

		// the protection param is used to protect the secret entry
		KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(secretEntryPassCharArray);


		KeyStore.Entry privateKeyEntry = new KeyStore.PrivateKeyEntry(key, new X509Certificate[]{x509Certificate});

		// adding the entry to the keystore
		keyStore.setEntry(secretEntryAliasName, privateKeyEntry, protectionParam); //"secretKeyAlias"

		//Storing the KeyStore object
		File file = new File("%s%s/%s.pfx".formatted(keyStoreFolderPath, secretEntryAliasName, keyStoreName));
		java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
		keyStore.store(fos, keyStorePassCharArray);

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
