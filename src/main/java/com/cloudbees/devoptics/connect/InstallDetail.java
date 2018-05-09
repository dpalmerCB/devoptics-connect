package com.cloudbees.devoptics.connect;

/**
 * Created by dpalmer on 4/3/18.
 */
public class InstallDetail {

    private String oauthClientId;
    private String baseUrl;
    private String clientKey;
    private String sharedSecret;

    public InstallDetail(String oauthClientId, String baseUrl, String clientKey, String sharedSecret) {
        this.oauthClientId = oauthClientId;
        this.baseUrl = baseUrl;
        this.clientKey = clientKey;
        this.sharedSecret = sharedSecret;
    }

    @Override
    public String toString() {
        return "oauthClientId: " + oauthClientId +
               ", baseUrl: " + baseUrl +
               ", clientKey: " + clientKey +
               ", sharedSecret: " + sharedSecret;
    }
}
