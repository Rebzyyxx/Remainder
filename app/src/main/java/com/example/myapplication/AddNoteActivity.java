package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText textEditText;
    private Button saveButton;
    private Button setReminderButton;

    private long reminderTime = -1;

    private void setReminder(long reminderTimeInMillis, String title, String text) {
        Intent intent = new Intent(getApplicationContext(), ReminderBroadcastReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent);
        }

        Log.d("AddNoteActivity", "Reminder set for title: " + title + ", text: " + text + ", reminderTime: " + reminderTimeInMillis);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        EditText reminderTimeEditText = findViewById(R.id.reminderTimeEditText);
        titleEditText = findViewById(R.id.noteTitle);
        textEditText = findViewById(R.id.noteText);
        setReminderButton = findViewById(R.id.set_reminder_button);
        saveButton = findViewById(R.id.saveNoteButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEditText.getText().toString();
                String text = textEditText.getText().toString();
                String reminderTimeString = reminderTimeEditText.getText().toString();

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date reminderTime = null;
                try {
                    reminderTime = format.parse(reminderTimeString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (reminderTime != null) {
                    setReminder(reminderTime.getTime(), title, text);
                }

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(text)) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("Заголовок", title);
                    resultIntent.putExtra("Текст", text);
                    resultIntent.putExtra("reminderTime", reminderTime != null ? reminderTime.getTime() : -1);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(AddNoteActivity.this, "Напишите заголовок и текст", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddNoteActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                reminderTime = calendar.getTimeInMillis();
                                setReminderButton.setText("Reminder Set");

                                Log.d("AddNoteActivity", "Reminder time set: " + calendar.getTime());
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });
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
}