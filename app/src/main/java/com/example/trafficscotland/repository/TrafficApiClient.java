package com.example.trafficscotland.repository;

import android.util.Base64;
import android.util.Log;

import com.example.trafficscotland.BuildConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TrafficApiClient {

    public String downloadXml(String urlString)
            throws Exception {

        Log.d(
                "TrafficAPI",
                "Starting download from: " + urlString
        );

        String clientId =
                BuildConfig.TRAFFIC_CLIENT_ID.trim();

        String clientKey =
                BuildConfig.TRAFFIC_CLIENT_KEY.trim();

        // Safe debugging - only confirms whether
        // credentials have actually loaded.
        Log.d(
                "TrafficAPI",
                "Client ID loaded: "
                        + !clientId.isEmpty()
                        + " length="
                        + clientId.length()
        );

        Log.d(
                "TrafficAPI",
                "Client Key loaded: "
                        + !clientKey.isEmpty()
                        + " length="
                        + clientKey.length()
        );

        if (clientId.isEmpty() ||
                clientKey.isEmpty()) {

            throw new Exception(
                    "Traffic Scotland credentials are missing"
            );
        }

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("GET");

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                15000
        );

        // We want to see redirects rather
        // than silently following them.
        connection.setInstanceFollowRedirects(
                false
        );

        connection.setRequestProperty(
                "Accept",
                "application/xml,text/xml,*/*"
        );

        String credentials =
                clientId
                        + ":"
                        + clientKey;

        String encodedCredentials =
                Base64.encodeToString(
                        credentials.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        Base64.NO_WRAP
                );

        // Traffic Scotland coursework
        // authentication format:
        // Base64(clientId:clientKey)
        connection.setRequestProperty(
                "Authorization",
                encodedCredentials
        );

        int responseCode =
                connection.getResponseCode();

        Log.d(
                "TrafficAPI",
                "HTTP response: "
                        + responseCode
        );

        String contentType =
                connection.getContentType();

        Log.d(
                "TrafficAPI",
                "Content-Type: "
                        + contentType
        );

        if (responseCode >= 300 &&
                responseCode < 400) {

            String location =
                    connection.getHeaderField(
                            "Location"
                    );

            Log.e(
                    "TrafficAPI",
                    "Redirected to: "
                            + location
            );

            connection.disconnect();

            throw new Exception(
                    "Traffic Scotland redirected request"
            );
        }

        if (responseCode !=
                HttpURLConnection.HTTP_OK) {

            connection.disconnect();

            throw new Exception(
                    "Traffic Scotland returned HTTP "
                            + responseCode
            );
        }

        InputStream inputStream =
                connection.getInputStream();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder response =
                new StringBuilder();

        String line;

        while ((line =
                reader.readLine()) != null) {

            response.append(line);
            response.append("\n");
        }

        reader.close();
        inputStream.close();
        connection.disconnect();

        Log.d(
                "TrafficAPI",
                "Downloaded characters: "
                        + response.length()
        );

        int previewLength =
                Math.min(
                        300,
                        response.length()
                );

        if (previewLength > 0) {

            Log.d(
                    "TrafficAPI",
                    "Response preview: "
                            + response
                            .substring(
                                    0,
                                    previewLength
                            )
                            .replace(
                                    "\n",
                                    " "
                            )
            );
        }

        String result =
                response.toString().trim();

        if (result.startsWith(
                "<!DOCTYPE html")
                ||
                result.startsWith(
                        "<html")) {

            throw new Exception(
                    "Traffic Scotland returned HTML instead of XML"
            );
        }

        return response.toString();
    }
}