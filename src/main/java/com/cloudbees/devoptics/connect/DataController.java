package com.cloudbees.devoptics.connect;

import com.atlassian.connect.spring.AddonInstalledEvent;
import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.IgnoreJwt;

//import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
//import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.cloudbees.devoptics.connect.authentication.OAuth1Authentication;
import com.cloudbees.devoptics.connect.authentication.JwtAssertionToken;
import com.cloudbees.devoptics.connect.authentication.OAuth2Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import org.json.JSONArray;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by dpalmer on 4/3/18.
 */
@Controller
public class DataController {

    private static final String PROJECTS_ENDPOINT = "/rest/api/2/project";
    private static final String ISSUES_ENDPOINT = "/rest/api/2/issue/ticketId";
    //private static final String ALL_USERS_ENDPOINT = "/rest/api/latest/user/search?startAt=0&amp;maxResults=1000&amp;username=admin";
    private static final String ALL_USERS_ENDPOINT = "/rest/api/2/user/assignable/search?project=ANALYTICS";

    private static final RequestHandler requestHandler = new RequestHandler();

    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;


    @RequestMapping(value = "/devoptics/oauth1/projects", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @IgnoreJwt
    public String getProjectsOAuth1() {
        String temporaryToken = "W9CXJChuIUn1XrUUqOt5GGPlSraQmcLC";
        String secret = "vwRhNT";
        String consumerKey = "OauthKey";
        String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAL8EJ7U1yvZwajbyDfwXwd9GHnI8Gk0N9+PyUQkmrtR1+7/z4PPjnWeqoCtd90+9C556Tix7FKDNq3j4P1GI4mEeXhg5cAFGcCrWwz+ELqeauI7EuOt/ZaND6ojVJkRRxXpJCEzN4U6fkay/Ss3TGKpSG6WszTB2hf/rDhh9bZllAgMBAAECgYBP4B/uocDujpGLympYqWKTyBGBOWrSH+4x7qk+R2PCSlfsq8G8msi+idYSbGe3e1j5ezFnXkyA9aAnpL9ti+0FcDGfdWA/HjjEMS1e1b+qkQGG7CZQ4uwCPTIlZMxDhG+RQe6xIwMkEZu7cuWbe8IL4xY8wA/uWbisntMGhpRnXQJBAO1twRoKUqQizs0kv/F7GVK8LX9TQznHtmfClj1Up5i6T8eqijiUPhBxH9r/bPIRgMFSlrZhsGG7xSOCilWWLL8CQQDN9QrSigsgted3niSr0Mz2/G1mMUB/HSv34w8Vr//A3vJn4ixZTud4AlcBQvCLtVg+PA+8MFjSnZMgGsOx1S7bAkEA6XNsViVmQpszCcSjslWefZonhjUhOZPkBzvuK5msPOCchy9pPt6L8/C3KMbToWSSXAPPyr5dBovw98xoJmVF0wJBAKqVf/47udXA9FM916+7RFh5YP8YYgwmlEk/djoHSPRtdCcYzJS35r2eaoOv9t4wOENrqbi77oWbX8VihZ2gLasCQHtAKkZlYhkO+y/WTw3C9yi2oofw31ycSGnd4Lk4n/A8utBe56l8raymGCoxpofUFm/sib2MHJOsY7MwOEyW8mg\\=";

        OAuthParameters parameters = null;

        try {
            OAuth1Authentication OAuth1Authentication = new OAuth1Authentication("https://dpalmer-cloudbees.atlassian.net" + "/rest/api/2/project");
            try {
                parameters = OAuth1Authentication.getParameters(temporaryToken, secret, consumerKey, privateKey);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(parameters);
            HttpRequest request = requestFactory.buildGetRequest(new GenericUrl("https://dpalmer-cloudbees.atlassian.net" + "/rest/api/2/project"));
            HttpResponse httpResponse = request.execute();
            //parseResponse(httpResponse);

            try (InputStream gzip = httpResponse.getContent();
                 Reader decoder = new InputStreamReader(gzip);
                 BufferedReader buffered = new BufferedReader(decoder)) {

                String test = buffered.readLine();

                if (test != null) {
                    return test;
                }

                JsonNode tree = null;
                if (!tree.isArray()) {
                    return "empty tree";
                }
                Set<String> keys = new TreeSet<>();
                String debugOutput = "";
                for (JsonNode project : tree) {
                    keys.add(project.get("key").asText());
                    debugOutput += project.get("key").asText();
                }
                //return keys;
                return debugOutput;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }

            return "failed";

        } catch (IOException e) {
            return e.toString();
        }

    }

    private static void parseResponse(com.google.api.client.http.HttpResponse response) throws IOException {
        Scanner s = new Scanner(response.getContent()).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";

        try {
            JSONArray jsonArray = new org.json.JSONArray(result);
            System.out.println(jsonArray.toString(2));
        } catch (Exception e) {
            System.out.println(result);
        }
    }

    @RequestMapping(value = "/devoptics/users", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @IgnoreJwt
    public String getUsers() {
        String response = "nothing";
        try {
            response = getRequest("admin", ALL_USERS_ENDPOINT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/devoptics/projects", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @IgnoreJwt
    public String getProjects() {
        String response = "nothing";
        try {
            response = getRequest("admin", PROJECTS_ENDPOINT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/devoptics/issue/{ticketId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @IgnoreJwt
    public Object getIssue(@PathVariable("ticketId") String ticketId) {

        String response = "nothing";
        try {
            response = getRequest("admin", ISSUES_ENDPOINT.replace("ticketId", ticketId));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String getRequest(String userKey, String restPath) throws Exception {
        String host = getHost(userKey);
        String testResource = "/rest/api/2/project";
        String url = host + restPath;

        try {
            JwtAssertionToken jwtAssertionToken = new JwtAssertionToken(userKey);
            OAuth2Token oAuth2Token = new OAuth2Token(jwtAssertionToken);

            System.out.println("\nMAKING REQUEST AS USER to " + url + " as " + userKey);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + oAuth2Token.getToken());
            headers.put("Accept", "application/json");
            String requestAsUserResponse = requestHandler.get(url, headers);

            return requestAsUserResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private String getHost(String userKey) {
        return "https://dpalmer-cloudbees.atlassian.net";
    }


    // TODO: I am ignoring concurrency issues with this for the moment
    private static final Map<String, InstallDetail> installDetails = new HashMap<>();

    @EventListener
    public void addonInstalled(AddonInstalledEvent event) {
        AtlassianHost atlassianHost = event.getHost();

        if (atlassianHost.isAddonInstalled()) {
            String oauthClientId = atlassianHost.getOauthClientId();
            String baseUrl = atlassianHost.getBaseUrl();
            String clientKey = atlassianHost.getClientKey();
            String sharedSecret = atlassianHost.getSharedSecret();

            InstallDetail installDetail = new InstallDetail(oauthClientId, baseUrl, clientKey, sharedSecret);
            System.out.println(installDetail);

            installDetails.put(baseUrl, installDetail);
        }
    }

    public static InstallDetail getInstallDetail(String baseUrl) {
        return installDetails.get(baseUrl);
    }

    public static String getInstallDetails() {
        String output = "";
        for (InstallDetail installDetail : installDetails.values()) {
            output = output + installDetail.toString() + "\n";
        }
        return output;
    }
}
