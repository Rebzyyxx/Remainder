package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText textEditText;
    private Button saveButton;
    private Button setReminderButton;

    private long reminderTime = -1;

    private void setReminder(long reminderTimeInMillis, String title, String text) {
        Intent intent = new Intent(getApplicationContext(), ReminderBroadcastReceiver.class);
        intent.putExtra("Заголовок", title);
        intent.putExtra("Текст", text);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent);
        }

        Log.d("AddNoteActivity", "Reminder set for title: " + title + ", Текст: " + text + ", reminderTime: " + reminderTimeInMillis);
    }

    private void createNotification(String title, String text, long reminderTime) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my_channel_id")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (reminderTime > System.currentTimeMillis()) {
            builder.setWhen(reminderTime);
            builder.setShowWhen(true);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

        Log.d("AddNoteActivity", "Notification created for title: " + title + ", text: " + text + ", reminderTime: " + reminderTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        Button backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        titleEditText = findViewById(R.id.noteTitle);
        textEditText = findViewById(R.id.noteText);
        setReminderButton = findViewById(R.id.set_reminder_button);
        saveButton = findViewById(R.id.saveNoteButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEditText.getText().toString();
                String text = textEditText.getText().toString();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(text)) {
                    Note note = new Note(title, text);
                    note.setReminderTime(new Date(reminderTime));

                    List<Note> noteList = getNoteListFromStorage();
                    noteList.add(note);
                    saveNoteListToStorage(noteList);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("Заголовок", title);
                    resultIntent.putExtra("Текст", text);
                    resultIntent.putExtra("reminderTime", reminderTime);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(AddNoteActivity.this, "Напишите заголовок и текст", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddNoteActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Calendar selectedDate = Calendar.getInstance();
                                selectedDate.set(year, month, dayOfMonth);
                                String buttonText = "Напоминание: " + dayOfMonth + "/" + (month + 1) + "/" + year;
                                setReminderButton.setText(buttonText);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddNoteActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                reminderTime = calendar.getTimeInMillis();
                                setReminderButton.setText("Напоминание");

                                Log.d("AddNoteActivity", "Reminder time set: " + calendar.getTime());
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }

        });
    }

    private static final String NOTE_PREFS_NAME = "NotePrefs";
    private static final String NOTE_LIST_KEY = "NoteList";


    private List<Note> getNoteListFromStorage() {
        SharedPreferences sharedPreferences = getSharedPreferences(NOTE_PREFS_NAME, Context.MODE_PRIVATE);
        String noteListJson = sharedPreferences.getString(NOTE_LIST_KEY, null);
        if (noteListJson != null) {
            Type listType = new TypeToken<List<Note>>() {}.getType();
            return new Gson().fromJson(noteListJson, listType);
        } else {
            return new ArrayList<>();
        }
    }

    private void saveNoteListToStorage(List<Note> noteList) {
        SharedPreferences sharedPreferences = getSharedPreferences(NOTE_PREFS_NAME, Context.MODE_PRIVATE);
        String noteListJson = new Gson().toJson(noteList);
        sharedPreferences.edit().putString(NOTE_LIST_KEY, noteListJson).apply();
    }
}