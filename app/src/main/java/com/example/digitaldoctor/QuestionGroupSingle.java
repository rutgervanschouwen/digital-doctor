package com.example.digitaldoctor;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitaldoctor.models.Evidence;
import com.example.digitaldoctor.models.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionGroupSingle extends AppCompatActivity {

    TextView title;
    RadioGroup radioGroup;
    Button nextQuestionBtn;
    JSONObject session;
    JSONObject question;
    String sessionId;

    AppDatabase db;
    SessionDao dao;

    boolean isDarkModeEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_group_single);

        title = (TextView) findViewById(R.id.questionTitle);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        nextQuestionBtn = (Button) findViewById(R.id.nextQuestionBtn);

        // Set the toolbar text
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Interview");

        // Change colors of the button so that it looks disabled
        Utils.disableNextBtn(getApplicationContext(), nextQuestionBtn);

        isDarkModeEnabled = Utils.isDarkModeEnabled(getApplicationContext());

        db = AppDatabase.getInstance(this);
        dao = db.sessionDao();

        if(getIntent().hasExtra("sessionId")) {
            sessionId = getIntent().getStringExtra("sessionId");
        }

        if (getIntent().hasExtra("session")) {
            try {
                session = new JSONObject(getIntent().getStringExtra("session"));
                question = session.getJSONObject("question");
                title.setText(question.get("text").toString());

                JSONArray items = question.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {

                    RadioButton radioBtn = new RadioButton(getApplicationContext());
                    radioBtn.setText(items.getJSONObject(i).getString("name"));
                    radioBtn.setId(i);
                    if (isDarkModeEnabled) {
                        radioBtn.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        radioBtn.setTextColor(getResources().getColor(R.color.dark));
                    }
                    radioBtn.setTextSize(18);
                    radioBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Enable the next button
                            nextQuestionBtn.setEnabled(true);
                            nextQuestionBtn.setBackgroundColor(getResources().getColor(R.color.green));
                            nextQuestionBtn.setTextColor(getResources().getColor(R.color.dark));

                        }
                    });
                    radioGroup.addView(radioBtn);
                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        nextQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedId = radioGroup.getCheckedRadioButtonId();
                try {
                    JSONObject jsonObject = question.getJSONArray("items").getJSONObject(selectedId);
                    Evidence evidence = new Evidence(
                            sessionId,
                            jsonObject.getString("id"),
                            "present",
                            "initial",
                            jsonObject.getString("id")
                    );
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Session session = dao.getSession(getIntent().getStringExtra("sessionId"));
                            session.evidenceList.add(evidence);
                            dao.updateSession(session);
                        }
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    finish();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


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