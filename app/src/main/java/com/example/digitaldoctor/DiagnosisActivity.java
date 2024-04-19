package com.example.digitaldoctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DiagnosisActivity extends AppCompatActivity {

    String sessionId;
    AppDatabase db;
    SessionDao dao;
    JSONObject session;

    LinearLayout conditionLayout;
    ConstraintLayout hasEmergencyLayout;
    Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis);

        // Set the toolbar text
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Diagnosis");

        db = AppDatabase.getInstance(this);
        dao = db.sessionDao();

        // Initialize views
        conditionLayout = (LinearLayout) findViewById(R.id.conditionLayout);
        hasEmergencyLayout = (ConstraintLayout) findViewById(R.id.warningDetails);
        resetBtn = (Button) findViewById(R.id.resetBtn);

        if(getIntent().hasExtra("sessionId")) {
            sessionId = getIntent().getStringExtra("sessionId");
        }

        if (getIntent().hasExtra("session")) {

            if (!getIntent().getBooleanExtra("hasEmergencyEvidence", false)) {
                ViewGroup parent = (ViewGroup) hasEmergencyLayout.getParent();
                parent.removeView(hasEmergencyLayout);
            }

            try {
                session = new JSONObject(getIntent().getStringExtra("session"));
                JSONArray conditions = session.getJSONArray("conditions");

                int threshold = Math.min(conditions.length(), 3);
                for (int i = 0; i < threshold; i++) {

                    // Condition content
                    JSONObject condition = conditions.getJSONObject(i);
                    String conditionName = condition.getString("name");
                    String conditionCommonName = condition.getString("common_name");
                    int conditionProgress = (int) (condition.getDouble("probability") * 100);

                    // Create a new conditionView
                    View conditionView = getLayoutInflater().inflate(R.layout.condition, null);
                    CircularProgressIndicator progressIndicator = (CircularProgressIndicator) conditionView.findViewById(R.id.progressIndicator);
                    TextView conditionTitle = (TextView) conditionView.findViewById(R.id.conditionTitle);
                    TextView conditionTitleAKA = (TextView) conditionView.findViewById(R.id.conditionTitleAKA);
                    TextView conditionNum = (TextView) conditionView.findViewById(R.id.progressNum);

                    // Set the content
                    conditionTitle.setText(conditionName);
                    if (!conditionName.equals(conditionCommonName)) {
                        conditionTitleAKA.setText(conditionCommonName);
                    } else {
                        conditionTitleAKA.setHeight(0);
                    }
                    String progressString = conditionProgress + "%";
                    conditionNum.setText(progressString);
                    progressIndicator.setProgressCompat((int) conditionProgress, false);
                    conditionLayout.addView(conditionView);
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        }

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.resetInterview(dao, sessionId);
                Intent reset = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(reset);
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