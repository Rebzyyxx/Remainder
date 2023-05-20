package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Note> notes = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = new DataManager(this);
        notes = dataManager.loadNotes();
        noteAdapter = new NoteAdapter(this, notes);

        ListView noteListView = findViewById(R.id.list);
        noteListView.setAdapter(noteAdapter);

        createNotificationChannel();


        Button addNoteButton = findViewById(R.id.addNote);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivityForResult(intent, 1);
            }
        });


        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = (Note) parent.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, NoteDetailsActivity.class);
                intent.putExtra("Заголовок", note.getTitle());
                intent.putExtra("Текст", note.getDescription());
                startActivity(intent);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "My Channel";
            String channelDescription = "Description of My Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("my_channel_id", channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String title = data.getStringExtra("Заголовок");
                String text = data.getStringExtra("Текст");
                long reminderTime = data.getLongExtra("reminderTime", -1);

                if (title != null && text != null) {
                    Note note = new Note();
                    note.setTitle(title);
                    note.setText(text);
                    note.setReminderTime(new Date(reminderTime));

                    // Инициализируем список notes, если он был null
                    if (notes == null) {
                        notes = new ArrayList<>();
                    }

                    notes.add(note);
                    noteAdapter.notifyDataSetChanged();
                    dataManager.saveNotes(notes);
                } else {
                    Toast.makeText(this, "Получены неверные данные", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}




