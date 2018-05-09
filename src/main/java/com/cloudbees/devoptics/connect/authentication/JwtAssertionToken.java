package com.cloudbees.devoptics.connect.authentication;

import com.cloudbees.devoptics.connect.RequestHandler;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import static org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256;

/**
 * Created by dpalmer on 4/4/18.
 */
public class JwtAssertionToken {

    public static final String AUTHORIZATION_SERVER_URL = "https://auth.atlassian.io";
    public static final String JWT_ISSUER_PREFIX = "urn:atlassian:connect:clientid:";
    public static final String JWT_SUBJECT_PREFIX = "urn:atlassian:connect:userkey:";
    public static final String JWT_TNT_CLAIM = "tnt";
    public static final String HS256 = "HS256";
    public static final String JWT = "JWT";

    public static final long EXPIRY_MILLISECONDS = 10000;

    private RequestHandler requestHandler;
    private String jwtToken;

    //
    // Debug hardcoded things
    // We will store these strings for each customer when they onboard
    //
    private String baseUrl = "https://dpalmer-cloudbees.atlassian.net";
    private String clientId = "eyJob3N0S2V5IjoiMzdhZTRhN2ItOTliMS0zMjQ2LWFiNmYtNzIyYTM2YmM3NTI4IiwiYWRkb25LZXkiOiJkZXZvcHRpY3MtY29ubmVjdCJ9";
    private String secret = "wpzdVWRJec/TPTMT1NoHI3GYYcSVNS3Z05Pbf1xKOBK9fq/36mKRruS2CWLwjFdRBleE/0jmZHUhQRDV4zgfbw";

    public JwtAssertionToken(String userKey) throws InvalidKeyException, NoSuchAlgorithmException, JoseException {
        requestHandler = new RequestHandler();
        JwtClaims jwtClaims = createJwt(clientId, userKey, baseUrl);
        JsonWebSignature jsonWebSignature = createJws(jwtClaims, secret);
        jwtToken = jsonWebSignature.getCompactSerialization();
    }

    public String getToken() {
        return jwtToken;
    }

    private JwtClaims createJwt(String clientId, String userKey, String url) {
        JwtClaims jwtClaims = new JwtClaims();
        long now = System.currentTimeMillis();

        jwtClaims.setIssuer(JWT_ISSUER_PREFIX + clientId);
        jwtClaims.setSubject(JWT_SUBJECT_PREFIX + userKey);
        jwtClaims.setClaim(JWT_TNT_CLAIM, url);
        jwtClaims.setAudience(AUTHORIZATION_SERVER_URL);
        jwtClaims.setIssuedAt(NumericDate.fromMilliseconds(now));
        jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(now + EXPIRY_MILLISECONDS));

        return jwtClaims;
    }

    private JsonWebSignature createJws(JwtClaims jwtClaims, String secret) {
        JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setHeader(HeaderParameterNames.ALGORITHM, HS256);
        jsonWebSignature.setHeader(HeaderParameterNames.TYPE, JWT);
        jsonWebSignature.setPayload(jwtClaims.toJson());
        jsonWebSignature.setKey(new HmacKey(secret.getBytes()));
        jsonWebSignature.setAlgorithmHeaderValue(HMAC_SHA256);

        return jsonWebSignature;
    }
}
