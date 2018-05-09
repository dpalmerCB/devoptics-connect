package com.cloudbees.devoptics.connect.authentication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dpalmer on 4/4/18.
 */
public class OAuth2Token {

    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String GRANT_TYPE_VALUE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    public static final String SCOPES_KEY = "scope";
    public static final String SCOPES_VALUE = "READ ACT_AS_USER";     // we should get this from the config
    public static final String ASSERTION_KEY = "assertion";

    public static final String AUTHORIZATION_SERVER_URL = "https://auth.atlassian.io/oauth2/token";
    public static final String POST = "POST";

    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String CONTENT_LENGTH_KEY = "Content-Length";
    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded;charset=";

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private String oAuthToken;

    public OAuth2Token(JwtAssertionToken jwtAssertionToken) throws IOException, JSONException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(GRANT_TYPE_KEY, GRANT_TYPE_VALUE);
        parameters.put(SCOPES_KEY, SCOPES_VALUE);
        parameters.put(ASSERTION_KEY, jwtAssertionToken.getToken());
        String accessTokenResponseBody = post(AUTHORIZATION_SERVER_URL, parameters);
        oAuthToken = getAccessTokenFromResponseBody(accessTokenResponseBody);
    }

    public String getToken() {
        return oAuthToken;
    }

    private static String post(String url, Map<String, String> formParameters) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(POST);
        connection.setDoOutput(true);
        connection.setRequestProperty(ACCEPT_CHARSET, UTF8.name());
        connection.setRequestProperty(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE + UTF8.name());

        StringBuilder formBodyBuilder = new StringBuilder();
        Boolean firstParam = true;
        for (Map.Entry<String, String> param : formParameters.entrySet()) {
            if (!firstParam) {
                formBodyBuilder.append("&");
            } else {
                firstParam = false;
            }
            formBodyBuilder
                .append(URLEncoder.encode(param.getKey(), UTF8.name()))
                .append("=")
                .append(URLEncoder.encode(param.getValue(), UTF8.name()));
        }

        byte[] formBody = formBodyBuilder.toString().getBytes(UTF8);
        connection.setRequestProperty(CONTENT_LENGTH_KEY, Integer.toString(formBody.length));
        new DataOutputStream(connection.getOutputStream()).write(formBody);
        int responseCode = connection.getResponseCode();

        if (responseCode >= 400) {
            String errorLine;
            final InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }
            }
        }
        final InputStream inputStream = connection.getInputStream();
        if (inputStream == null) {
            return "";
        }
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = responseReader.readLine()) != null) {
            sb.append(line.trim());
        }
        return sb.toString();
    }

    private String getAccessTokenFromResponseBody(String accessTokenResponseBody) throws JSONException {
        JSONObject json = new JSONObject(accessTokenResponseBody);
        return json.getString("access_token");
    }
}
