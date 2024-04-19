package com.example.digitaldoctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.room.Database;

import com.android.volley.VolleyError;
import com.example.digitaldoctor.adapters.EvidenceAdapter;
import com.example.digitaldoctor.adapters.SearchResultAdapater;
import com.example.digitaldoctor.models.Evidence;
import com.example.digitaldoctor.models.Session;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Button nextBtn;

    AppDatabase db;
    SessionDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        dao = db.sessionDao();

        SharedPreferences sharedPref = this.getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Check if there is already a session, if not, it returns null
        Session session = Utils.setSharedPreferences(this, dao);

        if(session != null) {

            Intent redirect;
            if (session.evidenceList.size() > 0) {
                // If there is already evidence added, redirect to the interview
                redirect = new Intent(getApplicationContext(), InterviewActivity.class);
            } else {
                // If there is no evidence added yet, redirect to search
                redirect = new Intent(getApplicationContext(), SearchActivity.class);
            }
            startActivity(redirect);

        } else {

            nextBtn = (Button) findViewById(R.id.nextBtn);

            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent start = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(start);
                }
            });

        }

    }

}