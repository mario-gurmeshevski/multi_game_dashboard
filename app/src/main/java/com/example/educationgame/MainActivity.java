package com.example.educationgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout schedulerButton, logicButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        schedulerButton = findViewById(R.id.schedulerButton);
        logicButton = findViewById(R.id.logicButton);

        schedulerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SchedulerActivity.class);
            startActivity(intent);
        });

        logicButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogicActivity.class);
            startActivity(intent);
        });
    }
}