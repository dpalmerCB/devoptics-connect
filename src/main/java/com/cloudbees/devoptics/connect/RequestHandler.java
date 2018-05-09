package com.cloudbees.devoptics.connect;

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
 * Created by dpalmer on 4/5/18.
 */
public class RequestHandler {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static String get(String url, Map<String, String> headers) {
        try {
            return makeRequest(url, headers, new HashMap<>());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String makeRequest(String url, Map<String, String> headers, Map<String, String> formParameters)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
        String method = formParameters.isEmpty() ? "GET" : "POST";
        System.out.println(method + " to " + url);
        connection.setRequestMethod(method);
        if (method.equals("POST")) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", UTF8.name());
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + UTF8.name());
            StringBuilder formBodyBuilder = new StringBuilder();
            Boolean firstParam = true;
            for (Map.Entry<String, String> param : formParameters.entrySet()) {
                if (!firstParam) {
                    formBodyBuilder.append("&");
                } else {
                    firstParam = false;
                }
                formBodyBuilder.append(URLEncoder.encode(param.getKey(), UTF8.name()))
                        .append("=")
                        .append(URLEncoder.encode(param.getValue(), UTF8.name()));
            }
            final String formBodyString = formBodyBuilder.toString();
            System.out.println("form body:");
            System.out.println(formBodyString);
            byte[] formBody = formBodyString.getBytes(UTF8);
            connection.setRequestProperty("Content-Length", Integer.toString(formBody.length));
            new DataOutputStream(connection.getOutputStream()).write(formBody);
        }
        int responseCode = connection.getResponseCode();
        if (responseCode >= 400) {
            System.err.println("request failed with response code " + responseCode);
            String errorLine;
            final InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }
            }
            System.exit(3);
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
}
