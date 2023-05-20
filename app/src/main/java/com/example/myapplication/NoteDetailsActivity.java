package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public class NoteDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        Intent intent = getIntent();
        String title = intent.getStringExtra("Заголовок");
        String description = intent.getStringExtra("Текст");

        Log.d("NoteDetailsActivity", "Received title: " + title);
        Log.d("NoteDetailsActivity", "Received description: " + description);

        TextView titleView = findViewById(R.id.noteDetailsTitle);
        TextView descriptionView = findViewById(R.id.noteDescription);
        titleView.setText(title);
        descriptionView.setText(description);

        Button closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}