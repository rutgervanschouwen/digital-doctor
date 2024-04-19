package com.example.digitaldoctor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.digitaldoctor.models.Evidence;
import com.example.digitaldoctor.models.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuestionGroupMultiple extends AppCompatActivity {

    TextView title;
    LinearLayout questions;
    Button nextQuestionBtn;
    JSONObject session;
    JSONObject question;
    String sessionId;

    AppDatabase db;
    SessionDao dao;

    boolean isDarkModeEnabled;

    List<String> questionAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_group_multiple);

        // Initialize views
        title = (TextView) findViewById(R.id.questionTitle);
        questions = (LinearLayout) findViewById(R.id.checkboxGroup);
        nextQuestionBtn = (Button) findViewById(R.id.nextQuestionBtn);

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

                JSONArray items = question.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    // Initialize all questions as unanswered
                    questionAnswers.add(null);

                    // For every answer, create a button for every option
                    View answer = getLayoutInflater().inflate(R.layout.answer_item, null);
                    TextView answerTitleChild = answer.findViewById(R.id.answerTitle);
                    LinearLayout buttonLayout = answer.findViewById(R.id.buttonLayout);
                    answerTitleChild.setText(items.getJSONObject(i).getString("name"));

                    JSONArray choices = items.getJSONObject(i).getJSONArray("choices");
                    for (int j = 0; j < choices.length(); j++) {
                        JSONObject choice = choices.getJSONObject(j);
                        Button button = (Button) getLayoutInflater().inflate(R.layout.answer_toggle_button, null)
                                .findViewById(R.id.answerToggleButton);
                        button.setText(choice.getString("label"));
                        buttonLayout.addView(button);
                        int questionIdx = i;
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                toggleAnswer(buttonLayout, button, questionIdx);
                            }
                        });
                    }

                    questions.addView(answer);

                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        nextQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {

                    JSONArray jsonArray = question.getJSONArray("items");
                    for (int i = 0; i < questions.getChildCount(); i++) {
                        Evidence evidence = new Evidence(
                                sessionId,
                                jsonArray.getJSONObject(i).getString("id"),
                                questionAnswers.get(i),
                                "initial",
                                jsonArray.getJSONObject(i).getString("id")
                        );
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Session updateSession = dao.getSession(getIntent().getStringExtra("sessionId"));
                                updateSession.evidenceList.add(evidence);
                                dao.updateSession(updateSession);
                            }
                        });
                        thread.start();
                        thread.join();
                    }
                    finish();

                } catch (JSONException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }

    public void toggleAnswer(LinearLayout buttons, Button activeButton, int questionIdx) {

        for (int i = 0; i < buttons.getChildCount(); i++) {
            Button button = (Button) buttons.getChildAt(i).findViewById(R.id.answerToggleButton);
            if(isDarkModeEnabled) {
                button.setBackgroundColor(getResources().getColor(R.color.dark));
            } else {
                button.setBackgroundColor(getResources().getColor(R.color.gray));
            }
        }

        String answer = activeButton.getText().toString();
        String label = "";
        if (answer.equals("Yes")) {
            label = "present";
        } else if (answer.equals("No")) {
            label = "absent";
        } else if (answer.equals("Don't know")) {
            label = "unknown";
        }

        if(isDarkModeEnabled) {
            activeButton.setBackgroundColor(getResources().getColor(R.color.dark_2));
        } else {
            activeButton.setBackgroundColor(getResources().getColor(R.color.dark));
        }
        activeButton.setTextColor(getResources().getColor(R.color.white));
        questionAnswers.set(questionIdx, label);

        if (!questionAnswers.contains(null)) {
            // Enable the next button
            nextQuestionBtn.setEnabled(true);
            nextQuestionBtn.setBackgroundColor(getResources().getColor(R.color.green));
            nextQuestionBtn.setTextColor(getResources().getColor(R.color.dark));
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }
}
