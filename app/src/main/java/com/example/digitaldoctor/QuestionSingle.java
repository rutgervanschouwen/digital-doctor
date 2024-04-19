package com.example.digitaldoctor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitaldoctor.models.Evidence;
import com.example.digitaldoctor.models.Session;

import org.json.JSONException;
import org.json.JSONObject;

public class QuestionSingle extends AppCompatActivity {


    TextView title;
    RadioGroup radioGroup;
    Button nextQuestionBtn;
    Button yesBtn;
    Button noBtn;
    Button idkBtn;
    JSONObject session;
    JSONObject question;
    String sessionId;

    String choice_id;

    boolean isDarkModeEnabled;

    AppDatabase db;
    SessionDao dao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_single);

        title = (TextView) findViewById(R.id.questionTitle);
        nextQuestionBtn = (Button) findViewById(R.id.nextQuestionBtn);
        yesBtn = (Button) findViewById(R.id.yesBtn);
        noBtn = (Button) findViewById(R.id.noBtn);
        idkBtn = (Button) findViewById(R.id.idkBtn);

        isDarkModeEnabled = Utils.isDarkModeEnabled(getApplicationContext());

        // Set the toolbar text
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Interview");

        // Change colors of the button so that it looks disabled
        Utils.disableNextBtn(getApplicationContext(), nextQuestionBtn);

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
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        nextQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    JSONObject jsonObject = question.getJSONArray("items").getJSONObject(0);
                    Evidence evidence = new Evidence(
                            sessionId,
                            jsonObject.getString("id"),
                            choice_id,
                            "initial",
                            jsonObject.getString("id")
                    );
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Session session = dao.getSession(getIntent().getStringExtra("sessionId"));
                            session.evidenceList.add(evidence);
                            dao.updateSession(session);
//                            dao.addEvidence(evidence);
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

    public void toggleBtn(View v) {

        isToggled(yesBtn, false);
        isToggled(noBtn, false);
        isToggled(idkBtn, false);

        if (v.getId() == R.id.yesBtn) {
            isToggled(yesBtn, true);
            choice_id = "present";
        } else if (v.getId() == R.id.noBtn) {
            isToggled(noBtn, true);
            choice_id = "absent";
        } else if (v.getId() == R.id.idkBtn) {
            isToggled(idkBtn, true);
            choice_id = "unknown";
        } else {
            choice_id = "";
        }

        if (choice_id.length() > 0) {
            // Enable the next button
            nextQuestionBtn.setEnabled(true);
            nextQuestionBtn.setBackgroundColor(getResources().getColor(R.color.green));
            nextQuestionBtn.setTextColor(getResources().getColor(R.color.dark));
        }

    }

    public void isToggled(Button btn, Boolean active) {
        if (active) {
            if (isDarkModeEnabled) {
                btn.setBackgroundColor(getResources().getColor(R.color.dark_2));
                btn.setTextColor(getResources().getColor(R.color.white));
            } else {
                btn.setBackgroundColor(getResources().getColor(R.color.dark));
                btn.setTextColor(getResources().getColor(R.color.white));
            }
        } else {
            if (isDarkModeEnabled) {
                btn.setBackgroundColor(getResources().getColor(R.color.dark));
                btn.setTextColor(getResources().getColor(R.color.white));
            } else {
                btn.setBackgroundColor(getResources().getColor(R.color.gray));
                btn.setTextColor(getResources().getColor(R.color.dark));
            }
        }

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