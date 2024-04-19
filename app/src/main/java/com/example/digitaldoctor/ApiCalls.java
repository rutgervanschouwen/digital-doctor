package com.example.digitaldoctor;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.digitaldoctor.models.Evidence;
import com.example.digitaldoctor.models.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiCalls {

    String infermedicaUrl = "https://api.infermedica.com/v3/";

    public Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Dev-Mode", "true");
        headers.put("App-id", null); // Infermedica App-id
        headers.put("App-Key", null); // Infermedica App-Key
        return headers;
    }


    public void getRequest(Context context, String endpoint, @Nullable Map<String, String> headers, VolleyCallback callback) {

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                infermedicaUrl+endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            callback.onSucces(response);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error);
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                if(headers == null) {
                    return getDefaultHeaders();
                }
                headers.putAll(getDefaultHeaders());
                return headers;
            }
        };

        queue.add(request);

    }

    public void postRequest(Context context, String endpoint, JSONObject body, @Nullable Map<String, String> headers, VolleyCallback callback) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                infermedicaUrl + endpoint,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            callback.onSucces(response.toString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if(headers == null) {
                    return getDefaultHeaders();
                }
                headers.putAll(getDefaultHeaders());
                return headers;
            }
        };

        // Add the request to the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjectRequest);

    }

    public void search(Context context, String phrase, int age, VolleyCallback callback) {

        String endpoint = "search?phrase=" + phrase + "&age.value=" + age;
        getRequest(context, endpoint, null, callback);

    }

    public void getQuestion(Context context, SessionDao.SessionWithUserInfo session, List<Evidence> evidenceList, VolleyCallback callback) throws JSONException {

        String endpoint = "diagnosis";

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Interview-Id", session.id);

        JSONObject body = new JSONObject();
        JSONArray evidenceArray = new JSONArray();
        for (Evidence evidence : evidenceList) {
            JSONObject evidenceObj = new JSONObject();
            evidenceObj.put("id", evidence.getId());
            evidenceObj.put("choice_id", evidence.getChoice_id());
            evidenceObj.put("source", evidence.source);
            evidenceArray.put(evidenceObj);
        }

        body.put("evidence", evidenceArray);
        body.put("sex", session.gender);

        JSONObject age = new JSONObject();
        age.put("value", session.age);
        body.put("age", age);

        postRequest(context, endpoint, body, headers, callback);

    }


}
