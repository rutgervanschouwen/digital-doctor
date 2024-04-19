package com.example.digitaldoctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.android.volley.VolleyError;
import com.example.digitaldoctor.adapters.EvidenceAdapter;
import com.example.digitaldoctor.adapters.SearchResultAdapater;
import com.example.digitaldoctor.models.Evidence;
import com.example.digitaldoctor.models.Session;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    AppDatabase db;
    SessionDao dao;
    Session session;
    SessionDao.SessionWithUserInfo sessionWithUserInfo;

    ListView EvidencesListView;
    ListView searchResultListView;
    EditText searchEditText;
    ImageButton searchBtn;
    Button nextBtn;

    String sessionId;

    ArrayList<Evidence> initialEvidence = new ArrayList<Evidence>();

    public ArrayList<String> getAllEvidenceId() {
        ArrayList<String> idList = new ArrayList<String>();
        for(Evidence Evidence : dao.getSession(session.id).evidenceList) {
            idList.add(Evidence.getId());
        }
        return idList;
    }

    public void showNextButton() {
        if(dao.getSession(session.id).evidenceList.size() > 0) {
            nextBtn.setVisibility(View.VISIBLE);
        } else {
            nextBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = AppDatabase.getInstance(this);
        dao = db.sessionDao();

        // Find the current session, if none found, send the user back to the start
        session = Utils.setSharedPreferences(this, dao);
        if (session == null) {
             finish();
        }
        sessionId = session.id;
        sessionWithUserInfo = dao.getSessionWithUserInfo(session.userId);

        // Set the toolbar text
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Welcome, " + sessionWithUserInfo.name.substring(0, 1).toUpperCase() + sessionWithUserInfo.name.substring(1));

        // The class for using all the api calls
        ApiCalls api = new ApiCalls();

        // Initialize views
        searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchBtn = (ImageButton) findViewById(R.id.searchBtn);
        searchResultListView = (ListView) findViewById(R.id.searchResultListView);
        EvidencesListView = (ListView) findViewById(R.id.EvidenceListView);
        nextBtn = (Button) findViewById(R.id.nextBtn);

        // Only show nextBtn when there is at least 1 evidence selected
        showNextButton();

        dao.getLiveSession(session.id).observe(this, new Observer<Session>() {
            @Override
            public void onChanged(Session session) {
                EvidenceAdapter EvidenceAdapter = new EvidenceAdapter(getApplicationContext(), session.evidenceList, dao);
                EvidencesListView.setAdapter(EvidenceAdapter);
                showNextButton();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if there is an internet connection
                if (!Utils.isConnectedToInternet(getApplicationContext())) {
                    Intent noConnectionIntent = new Intent(getApplicationContext(), NoConnectionActivity.class);
                    startActivity(noConnectionIntent);
                }

                String searchPhrase = searchEditText.getText().toString();
                api.search(getApplicationContext(), searchPhrase, sessionWithUserInfo.age, new VolleyCallback() {

                    @Override
                    public void onSucces(String result) throws JSONException {

                        ArrayList<Evidence> data = new ArrayList<Evidence>();
                        JSONArray jsonArray = new JSONArray(result);
                        for(int i=0; i < jsonArray.length(); i++) {
                            String id = jsonArray.getJSONObject(i).getString("id");
                            String label = jsonArray.getJSONObject(i).getString("label");
                            if(!getAllEvidenceId().contains(id)) {
                                Evidence evidence = new Evidence(session.id, id, "present", "initial", label);
                                data.add(evidence);
                            }
                        }

                        SearchResultAdapater adapter = new SearchResultAdapater(getApplicationContext(), data);
                        searchResultListView.setAdapter(adapter);
                        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Evidence result = Evidence.class.cast(adapter.getItem(position));
                                data.remove(adapter.getItem(position));
                                searchResultListView.setAdapter(adapter);

                                result.setSessionId(session.id);
                                session.evidenceList.add(result);
                                dao.updateSession(session);

                            }
                        });
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.d(TAG, "onError: " + error);
                    }

                });

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent interview = new Intent(getApplicationContext(), InterviewActivity.class);
                startActivity(interview);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.resetInterview) {
            Utils.resetInterview(dao, sessionId);
            Intent reset = new Intent(getApplicationContext(), SearchActivity.class);
            startActivity(reset);
            return true;
        } else if (item.getItemId() == R.id.editUser) {
            Intent edit = new Intent(getApplicationContext(), StartActivity.class);
            edit.putExtra("isEdit", true);
            edit.putExtra("sessionId", sessionId);
            startActivity(edit);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}