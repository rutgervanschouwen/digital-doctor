package com.example.digitaldoctor;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.example.digitaldoctor.models.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class InterviewActivity extends AppCompatActivity {
    private static final String TAG = "InterviewActivity";

    AppDatabase db;
    SessionDao dao;
    Session session;

    SessionDao.SessionWithUserInfo sessionWithUserInfo;

    HashMap<String, Class> questionViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);

        // Database
        db = AppDatabase.getInstance(this);
        dao = db.sessionDao();

        // The different views for different type of questions
        questionViews.put("single", QuestionSingle.class);
        questionViews.put("group_single", QuestionGroupSingle.class);
        questionViews.put("group_multiple", QuestionGroupMultiple.class);

        // Set the toolbar text
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Interview");

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if there is an internet connection
        if (!Utils.isConnectedToInternet(getApplicationContext())) {
            Intent noConnectionIntent = new Intent(this, NoConnectionActivity.class);
            startActivity(noConnectionIntent);
        }

        ApiCalls api = new ApiCalls();

        // Check if there is already a session, if not, create one
        session = Utils.setSharedPreferences(this, dao);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sessionWithUserInfo = dao.getSessionWithUserInfo(session.userId);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            api.getQuestion(this, sessionWithUserInfo, session.evidenceList, new VolleyCallback() {
                @Override
                public void onSucces(String result) throws JSONException {
                    JSONObject sessionJson = new JSONObject(result);

                    if (sessionJson.getBoolean("should_stop") || session.evidenceList.size() > 5) {

                        Intent diagnosisIntent = new Intent(getApplicationContext(), DiagnosisActivity.class);
                        diagnosisIntent.putExtra("session", sessionJson.toString());
                        diagnosisIntent.putExtra("sessionId", session.id);
                        startActivity(diagnosisIntent);

                    } else if (sessionJson.getBoolean("has_emergency_evidence")) {

                        Intent diagnosisIntent = new Intent(getApplicationContext(), DiagnosisActivity.class);
                        diagnosisIntent.putExtra("session", sessionJson.toString());
                        diagnosisIntent.putExtra("sessionId", session.id);
                        diagnosisIntent.putExtra("hasEmergencyEvidence", true);
                        startActivity(diagnosisIntent);

                    } else {
                        String questionType = sessionJson.getJSONObject("question").getString("type");
                        Intent question = new Intent(getApplicationContext(), questionViews.get(questionType));
                        question.putExtra("session", sessionJson.toString());
                        question.putExtra("sessionId", session.id);
                        startActivity(question);
                    }

                }

                @Override
                public void onError(VolleyError error) {
                    if (Utils.isConnectedToInternet(getApplicationContext())) {
                        Log.d(TAG, "onError: " + error.networkResponse.allHeaders);
                    }
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
