package com.example.danie.schoolcashless.model;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;

import org.json.JSONArray;
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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by shihern on 29/7/2016.
 */
public class UserSession {
    /**
     * The API endpoint
     */
    public static final String ENDPOINT = "https://pckt.makerforce.io/api";
    private static UserSession userSession = null;
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
            HttpsURLConnection connection = (HttpsURLConnection)new URL(ENDPOINT + "/auth").openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Accept-Version", APIVER);
            connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));
            connection.connect();
            int status = connection.getResponseCode();
            if (status == 200) {
                requestUser();
            } else if (status == 401) {
                throw new BadAuthenticationException();
            } else {
                throw new BadResponseException(status);
            }
        } catch (JSONException e) {
            e.printStackTrace(); // impossible
        }
    }

    public static UserSession createInstance(String username, String password) throws BadResponseException, BadAuthenticationException, IOException {
        userSession = new UserSession(username, password);
        return userSession;
    }

    public static UserSession getInstance() {
        return userSession;
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
            userProfile = new JSONObject(requestGet("/user"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // impossible
        }
    }

    /**
     * Create a new user
     *
     * @return true if request was successful
     * @throws BadResponseException
     * @throws BadAuthenticationException
     * @throws IOException
     */
    public static Boolean createUser(String name, String username, String password) throws IOException, BadResponseException, BadAuthenticationException{
        JSONObject data = new JSONObject();
        try {
            data.put("name", name);
            data.put("username", username);
            data.put("password", password);
            //data.put("avatar", new JSONObject().put("id", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestPostWithoutAuth("/users", data);

        return true;
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
     * @throws JSONException
     */
    public String getName() throws JSONException {
        //requestUser();
        return userProfile.getString("name");
    }

    /**
     * @return User's balance
     * @throws BadResponseException
     * @throws BadAuthenticationException
     * @throws IOException
     * @throws JSONException
     */
    public double getBalance() throws BadResponseException, BadAuthenticationException, IOException, JSONException {
        return new JSONObject(requestGet("/status")).getDouble("value");
    }

    /**
     * Return the user's transactions
     * <code><pre>[{
     * "_id": ObjectId,
     * "from": User,
     * "value": Number
     * }, ...]</pre></code>
     *
     * @return JSONObject containing data
     * @throws BadResponseException
     * @throws BadAuthenticationException
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray getTransactions(int from, int max) throws BadResponseException, BadAuthenticationException, IOException, JSONException{
        return new JSONArray(requestGet("/transactions?max=" + max + "&from=" + from));
    }

    /**
     * Response:
     * <code><pre>{
     "_id": ObjectId,
     "code": String, // whatever the QR needs to encode. Is equal to the _id above for now.
     }</pre></code>
     *
     * @param value The amount of money to send
     * @return JSON Object of the response
     * @throws BadAuthenticationException
     * @throws BadResponseException
     * @throws IOException
     */
    public JSONObject createSendTransaction(double value) throws BadAuthenticationException, BadResponseException, IOException{

        try {
            JSONObject data = new JSONObject();
            data.put("value", value);
            data.put("type", "send");
            return new JSONObject(requestPost("/transactions", data));
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Response:
     * <code><pre>{
     "_id": ObjectId,
     "code": String, // whatever the QR needs to encode. Is equal to the _id above for now.
     }</pre></code>
     *
     * @param value The amount of money to send
     * @return JSON Object of the response
     * @throws BadAuthenticationException
     * @throws BadResponseException
     * @throws IOException
     */
    public JSONObject createReceiveTransaction(double value) throws BadAuthenticationException, BadResponseException, IOException {
        try {
            JSONObject data = new JSONObject();
            data.put("value", value);
            data.put("type", "receive");
            return new JSONObject(requestPost("/transactions", data));
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get more details about a transaction.
     *
     * <code><pre>{
     "_id": ObjectId,
     "from": User, // would be you if you initiated the transaction
     "to": User, // would be you if you are the responder
     "confirmedfrom": Boolean,
     "confirmedto": Boolean
     "created": Number, // Unix time, in seconds
     "completed": Number, // Unix time, in seconds
     "summary": String, // optional, may or may not be set.
     "value": Number
     }</pre></code>
     *
     * @param id The id of the transaction
     * @return JSONObject of the returned data
     * @throws BadResponseException
     * @throws BadAuthenticationException
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject getTransactionDetails(String id) throws BadResponseException, BadAuthenticationException, IOException, JSONException {
        return new JSONObject(requestGet("/transactions/" + id));
    }

    /**
     * Checks if the qr code of the transaction with id has been scanned.
     *
     * @param id The id of the transaction
     * @return True if transaction has been scanned, false if not
     */
    public Boolean getTransactionScanned(String id) {
        try {
            JSONObject response = new JSONObject(requestGet("/transactions/" + id + "/scanned"));
            Boolean i = response.getBoolean("status");
            Log.d("scanned", id + i.toString());
            return i;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param id The id of the transaction
     * @return True if transaction scanned value was updated, false if not
     */
    public Boolean putTransactionScanned(String id) {
        try {
            JSONObject request = new JSONObject();
            request.put("scanned", true);
            requestPut("/transactions/" + id, request);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Checks whether transaction has been confirmed from -
     * whether the user from whom the money is coming out of
     * has confirmed the transaction.
     *
     * @param id The id of the transaction
     * @return True if transaction confirmed from, false if transaction now confirmed from
     */
    public Boolean transactionConfirmedFrom(String id) {
        try{
            JSONObject response = new JSONObject(requestGet("/transactions/" + id + "/confirmedfrom"));
            return response.getBoolean("status");
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Checks whether transaction has been confirmed to -
     * whether the user whom the money is going to
     * has confirmed the transaction.
     *
     * @param id The id of the transaction
     * @return True if transaction confirmed from, false if transaction now confirmed from
     */
    public Boolean transactionConfirmedTo(String id) {
        try{
            JSONObject response = new JSONObject(requestGet("/transactions/" + id + "/confirmedto"));
            return response.getBoolean("status");
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param id The id of the transaction
     * @param summary New summary of the transaction
     * @throws BadResponseException
     * @throws BadAuthenticationException
     * @throws IOException
     * @throws JSONException
     */
    public void putTransactionSummary(int id, String summary) throws BadResponseException, BadAuthenticationException, IOException, JSONException {
        requestPut("/transactions/" + id, new JSONObject().put("summary", summary));
    }

    /**
     * Updates whether the transaction has been confirmed by the from - the guy paying the money
     *
     * @param id The id of the transaction
     * @return True if confirmfrom successfully updated, false if not
     */
    public Boolean transactionConfirmFrom(String id, Boolean bool) {
        try {
            JSONObject json = new JSONObject();
            json.put("confirmfrom", bool);
            requestPut("/transactions/" + id, json);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    /**
     * Updates whether the transaction has been confirmed by the to - the guy getting the money
     *
     * @param id The id of the transaction
     * @return True if confirmto successfully updated, false if not
     */
    public Boolean transactionConfirmTo(String id, Boolean bool) {
        try {
            JSONObject json = new JSONObject();
            json.put("confirmto", bool);
            requestPut("/transactions/" + id, json);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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


    /**
     * Sends a POST request for {@code url} to {@code ENDPOINT}
     *
     * @param url Request path
     * @param data JSON data to attach with request
     * @return Response data
     * @throws IOException
     * @throws BadResponseException
     * @throws BadAuthenticationException
     */
    public String requestPost(String url, JSONObject data) throws IOException, BadResponseException, BadAuthenticationException {
        return requestMethod("POST", url, data);
    }


    /**
     * Sends a PUT request for {@code url} to {@code ENDPOINT}
     *
     * @param url Request path
     * @param data JSON data to attach with request
     * @return Response data
     * @throws IOException
     * @throws BadResponseException
     * @throws BadAuthenticationException
     */
    public String requestPut(String url, JSONObject data) throws IOException, BadResponseException, BadAuthenticationException {
        return requestMethod("PUT", url, data);
    }


    /**
     * Sends a POST request for {@code url} to {@code ENDPOINT}. Unlike requestPost() this
     * does not attach the authentication of the user.
     *
     * @param url Request path
     * @param data JSON data to attach with request
     * @return Response data
     * @throws IOException
     * @throws BadResponseException
     * @throws BadAuthenticationException
     */
    public static String requestPostWithoutAuth(String url, JSONObject data) throws IOException, BadResponseException, BadAuthenticationException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection)new URL(ENDPOINT + url).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Accept-Version", APIVER);
            connection.setRequestMethod("POST");

            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(data.toString());
            writer.flush();
            writer.close();
            os.close();

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
        return null;
    }

    private String requestMethod(String method, String url, JSONObject data) throws IOException, BadResponseException, BadAuthenticationException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection)new URL(ENDPOINT + url).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Accept-Version", APIVER);
            connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));
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
