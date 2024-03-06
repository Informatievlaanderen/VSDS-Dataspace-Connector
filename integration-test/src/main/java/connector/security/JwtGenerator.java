package connector.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class JwtGenerator {
	public static String generateJwtToken(String issuer, String subject, String audience, ECPublicKey publicKey, ECPrivateKey key) {
		Algorithm algorithm = Algorithm.ECDSA256(publicKey, key);

		return JWT.create()
				.withIssuer(issuer)
				.withSubject(subject)
				.withAudience(audience)
				.withExpiresAt(ZonedDateTime.now().plusYears(5).toInstant())
				.sign(algorithm);
	}
}
