package com.example.edugames;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button schedulerButton, logicButton;

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