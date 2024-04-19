package com.example.digitaldoctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoConnectionActivity extends AppCompatActivity {

    Button retryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);

        retryBtn = (Button) findViewById(R.id.retryBtn);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if there is an internet connection
                if (Utils.isConnectedToInternet(getApplicationContext())) {
                    finish();
                }
            }
        });
    }
}