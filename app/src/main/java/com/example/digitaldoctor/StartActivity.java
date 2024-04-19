package com.example.digitaldoctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.digitaldoctor.models.Session;
import com.example.digitaldoctor.models.User;

import org.json.JSONObject;

import java.util.List;

public class StartActivity extends AppCompatActivity {

    AppDatabase db;
    SessionDao dao;

    TextView startTitle;
    EditText nameEditText;
    EditText ageEditText;
    RadioGroup genderRadioGroup;
    Button submitBtn;
    ConstraintLayout warningDetails;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    User user;
    boolean isEdit;

    long newUserId;
    String sessionId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        db = AppDatabase.getInstance(this);
        dao = db.sessionDao();

        // Initialize views
        startTitle = (TextView) findViewById(R.id.startTitle);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        ageEditText = (EditText) findViewById(R.id.ageEditTextNumber);
        genderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        warningDetails = (ConstraintLayout) findViewById(R.id.warningDetails);

        // SharedPreferences to safe the session locally
        sharedPref = this.getSharedPreferences("SharedPref", MODE_PRIVATE);
        editor = sharedPref.edit();

        isEdit = false;

        if (getIntent().hasExtra("isEdit")) {
            // If there is already a user, get the user and edit it.
            sessionId = getIntent().getStringExtra("sessionId");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    user = dao.getUserBySessionId(sessionId);
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            isEdit = true;
            startTitle.setText(R.string.start_title_edit);
            nameEditText.setText(user.name);
            ageEditText.setText(String.valueOf(user.age));
            RadioButton activeRadio = (RadioButton) genderRadioGroup.getChildAt(user.gender == "male" ? 1 : 0);
            activeRadio.setChecked(true);

        } else {
            ViewGroup parent = (ViewGroup) warningDetails.getParent();
            parent.removeView(warningDetails);
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if there is an internet connection
                if (!Utils.isConnectedToInternet(getApplicationContext())) {
                    Intent noConnectionIntent = new Intent(getApplicationContext(), NoConnectionActivity.class);
                    startActivity(noConnectionIntent);
                }

                // Get the values
                RadioButton selectedRadioBtn = (RadioButton) findViewById(genderRadioGroup.getCheckedRadioButtonId());
                String name = nameEditText.getText().toString().toLowerCase();

                if (name != null || ageEditText.getText().toString() != null || selectedRadioBtn != null) {

                    int age = Integer.valueOf(ageEditText.getText().toString());
                    String gender = selectedRadioBtn.getText().toString().toLowerCase();

                    if (!isEdit) {
                        // Create a new user
                        User newUser = new User(name, age, gender);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                newUserId = dao.addUser(newUser);
                            }
                        });
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        // Create a new session
                        Session newSession = new Session(newUserId);
                        dao.addSession(newSession);

                        // Save it to the shared preferences
                        editor.putString("Interview-Id", newSession.id);
                        editor.apply();

                    } else {
                        // Edit current user
                        user.setName(name);
                        user.setAge(age);
                        user.setGender(gender);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                dao.updateUser(user);
                                Session session = dao.getSession(sessionId);
                                session.evidenceList.clear();
                                dao.updateSession(session);
                            }
                        });
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Go to the next step: searching symptoms
                    Intent search = new Intent(getApplicationContext(), SearchActivity.class);
                    startActivity(search);

                } else {
                    // If not all the fields are filled in show a toast message
                    Toast.makeText(getApplicationContext(), R.string.error_fields, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}