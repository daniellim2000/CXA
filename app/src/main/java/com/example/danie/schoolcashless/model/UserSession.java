package com.example.danie.schoolcashless.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by shihern on 29/7/2016.
 */
public class UserSession {
    /**
     * The API endpoint
     */
    public static final String ENDPOINT = "https://pckt.makerforce.io/api/students";
    private static UserSession userSession;
    public static final String APIVER = "~1";
    private final String username;
    private final String password;
    private JSONObject userProfile;

    /**
     * Initializes a user session, and checks account validity
     *
     * @param username Account email
     * @param password Account password
     */
    private UserSession(@NonNull String username, @NonNull String password) throws IOException, BadAuthenticationException, BadResponseException {
        this.username = username;
        this.password = password;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT + "/auth").openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Accept-Version", APIVER);
            connection.setRequestProperty("Authorization", username + ":" + password);
            connection.connect();
            int status = connection.getResponseCode();
            if (status == 200) {
                requestUser();
            } else if (status == 401) {
                throw new BadAuthenticationException();
            } else {
                throw new BadResponseException(status);
            }
        } catch (Exception e) {
            e.printStackTrace(); // impossible
        }
    }

    public static void createInstance(String username, String password) throws BadResponseException, BadAuthenticationException, IOException {
        userSession = new UserSession(username, password);
    }

    public static UserSession getInstance() {
        return userSession;
    }

    /**
     * Get the current session username
     *
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the current session password
     *
     * @return Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return User's full name
     * @throws BadResponseException
     * @throws BadAuthenticationException
     * @throws JSONException
     */
    public String getName() throws BadResponseException, BadAuthenticationException, JSONException {
        //requestUser();
        return userProfile.getString("name");
    }

    /**
     * Gets the user profile data
     *
     * @throws BadResponseException
     * @throws BadAuthenticationException
     * @throws IOException
     * @throws JSONException
     */
    private void requestUser() throws BadResponseException, BadAuthenticationException, IOException, JSONException {
        try {
            userProfile = new JSONObject(requestGet("/users/" + URLEncoder.encode(username, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // impossible
        }
    }

    /**
     * Sends a GET request for {@code url} to {@code ENDPOINT}
     *
     * @param url Request path
     * @return Response data
     * @throws IOException
     * @throws BadResponseException
     */
    public String requestGet(String url) throws IOException, BadResponseException, BadAuthenticationException {
        return requestMethod("GET", url, null);
    }

    public String requestMethod(String method, String url, JSONObject data) throws IOException, BadResponseException, BadAuthenticationException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT + url).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Version", APIVER);
            connection.setRequestProperty("Authorization", username + ":" + password);
            connection.setRequestMethod(method);

            if (data != null) {
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(data.toString());
                writer.flush();
                writer.close();
                os.close();
            }

            connection.connect();
            int status = connection.getResponseCode();
            if (status == 200) {
                // yay
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                return sb.toString();
            } else if (status == 401) {
                throw new BadAuthenticationException();
            } else {
                Log.e("Response code", String.valueOf(status));
                throw new BadResponseException(status);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace(); // impossible
        } catch (BadAuthenticationException e) {
            e.printStackTrace(); // should not happen
            throw e;
        }
        return null; // impossible
    }
}
