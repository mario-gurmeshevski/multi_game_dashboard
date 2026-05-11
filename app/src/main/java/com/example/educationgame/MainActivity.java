package com.example.educationgame;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.educationgame.data.local.AppDatabase;
import com.example.educationgame.data.local.entity.UserEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

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

        checkForExistingUser();
    }

    private void checkForExistingUser() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            int userCount = db.userDao().getUserCount();
            if (userCount == 0) {
                runOnUiThread(this::showCreateAccountDialog);
            }
        });
    }

    private void showCreateAccountDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_account);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }

        TextInputEditText inputUsername = dialog.findViewById(R.id.inputUsername);
        MaterialButton btnCreateAccount = dialog.findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v -> {
            String username = inputUsername.getText() != null ? inputUsername.getText().toString().trim() : "";

            if (username.isEmpty()) {
                inputUsername.setError("Username is required");
                return;
            }

            UserEntity user = new UserEntity();
            user.setUsername(username);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.userDao().insert(user);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            });
        });

        dialog.show();
    }
}
