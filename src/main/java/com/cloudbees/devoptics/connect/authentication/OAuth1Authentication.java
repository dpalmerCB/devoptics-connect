package com.cloudbees.devoptics.connect.authentication;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by dpalmer on 3/27/18.
 *
 * WIP blah blah
 * I'm thinking there's a good chance we are going to have to end up storing something just by the nature of how JIRA has this set up - my suspicion for now is:
 * 1) We'll walk through setting up the auth token with the customer during onboarding, so they can add our app (the Devoptics UI) to their JIRA instance
 *    (As an aside, even though this is onlt every going to GET data from them, the JIRA confirmation screen on the walkthrough I did informs you that you are giving the app
 *     read-write access. We should stress to customers made nervous by this how much we are NEVER going to write to their JIRA instance.)
 * 2) The auth token that gets generated in that interactive bit does not seem to be short-lived, in that to get another one would require more manual work from the
 *    customer. If that is the case we can store it in the properties index along with their JIRA server.
 * This might not be the solution we were looking for, but from what the Atlassian guy said that's what we may have to do, if this lets us get around the CORS issues.
 */
public class OAuth1Authentication {

    private static final String AUTHORIZATION_ENDPOINT = "/plugins/servlet/oauth/authorize";
    private static final String ACCESS_TOKEN_ENDPOINT = "/plugins/servlet/oauth/access-token";

    private final String baseUrl;
    private final String authorizationUrl;
    private final String accessTokenUrl;

    public OAuth1Authentication(String baseUrl) {
        this.baseUrl = baseUrl;
        this.authorizationUrl = baseUrl + AUTHORIZATION_ENDPOINT;
        this.accessTokenUrl = baseUrl + ACCESS_TOKEN_ENDPOINT;
    }

    /**
     * Gets temporary request token and creates url to authorize it
     *
     * @param consumerKey consumer key
     * @param privateKey  private key in PKCS8 format
     * @return request token value
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public AuthorizationTokenDetails getAndAuthorizeTemporaryToken(String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        OAuthGetTemporaryToken oAuthGetTemporaryToken = new OAuthGetTemporaryToken(authorizationUrl);
        oAuthGetTemporaryToken.consumerKey = consumerKey;

        oAuthGetTemporaryToken.signer = getOAuthRsaSigner(privateKey);
        oAuthGetTemporaryToken.transport = new ApacheHttpTransport();
        oAuthGetTemporaryToken.callback = "oob";

        OAuthCredentialsResponse response = oAuthGetTemporaryToken.execute();
        OAuthAuthorizeTemporaryTokenUrl oAuthAuthorizeTemporaryTokenUrl = new OAuthAuthorizeTemporaryTokenUrl(authorizationUrl);
        oAuthAuthorizeTemporaryTokenUrl.temporaryToken = response.token;

        AuthorizationTokenDetails authorizationTokenDetails = new AuthorizationTokenDetails(response.token, response.tokenSecret, oAuthAuthorizeTemporaryTokenUrl);
        return authorizationTokenDetails;
    }

    /**
     * Gets access token from JIRA
     *
     * @param temporaryToken    temporary request token
     * @param secret      secret (verification code provided by JIRA after request token authorization)
     * @param consumerKey consumer ey
     * @param privateKey  private key in PKCS8 format
     * @return access token value
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public String getAccessToken(String temporaryToken, String secret, String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        OAuthGetAccessToken oAuthGetAccessToken = getOAuthGetAccessToken(temporaryToken, secret, consumerKey, privateKey);
        OAuthCredentialsResponse response = oAuthGetAccessToken.execute();
        return response.token;
    }

    /**
     * Creates OAuthParameters used to make authorized request to JIRA
     *
     * @param temporaryToken
     * @param secret
     * @param consumerKey
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public OAuthParameters getParameters(String temporaryToken, String secret, String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        OAuthGetAccessToken oAuthGetAccessToken = getOAuthGetAccessToken(temporaryToken, secret, consumerKey, privateKey);
        return oAuthGetAccessToken.createParameters();
    }

    private OAuthGetAccessToken getOAuthGetAccessToken(String temporaryToken, String secret, String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        OAuthGetAccessToken oAuthGetAccessToken = new OAuthGetAccessToken(accessTokenUrl);
        oAuthGetAccessToken.consumerKey = consumerKey;
        oAuthGetAccessToken.signer = getOAuthRsaSigner(privateKey);
        oAuthGetAccessToken.transport = new ApacheHttpTransport();
        oAuthGetAccessToken.verifier = secret;
        oAuthGetAccessToken.temporaryToken = temporaryToken;
        oAuthGetAccessToken.put("user_id", "analyticsPnly1234");
        return oAuthGetAccessToken;
    }

    private OAuthRsaSigner getOAuthRsaSigner(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        OAuthRsaSigner oAuthRsaSigner = new OAuthRsaSigner();
        oAuthRsaSigner.privateKey = kf.generatePrivate(keySpec);
        return oAuthRsaSigner;
    }

    private class AuthorizationTokenDetails {
        private String temporaryToken;
        private String temporaryTokenSecret;
        private OAuthAuthorizeTemporaryTokenUrl oAuthAuthorizeTemporaryTokenUrl;

        public AuthorizationTokenDetails(String temporaryToken, String temporaryTokenSecret, OAuthAuthorizeTemporaryTokenUrl oAuthAuthorizeTemporaryTokenUrl) {
            this.temporaryToken = temporaryToken;
            this.temporaryTokenSecret = temporaryTokenSecret;
            this.oAuthAuthorizeTemporaryTokenUrl = oAuthAuthorizeTemporaryTokenUrl;
        }
    }

}
