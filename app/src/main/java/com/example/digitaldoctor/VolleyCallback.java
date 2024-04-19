package com.example.digitaldoctor;

import com.android.volley.VolleyError;

import org.json.JSONException;

public interface VolleyCallback {
    //    void onSucces(JSONArray result) throws JSONException;
    void onSucces(String result) throws JSONException;
    void onError(VolleyError error);
}
