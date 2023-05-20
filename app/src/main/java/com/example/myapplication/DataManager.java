package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DataManager {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_NOTES = "notes";

    private SharedPreferences sharedPreferences;

    public DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveNotes(List<Note> notes) {
        Gson gson = new Gson();
        String notesJson = gson.toJson(notes);
        sharedPreferences.edit().putString(KEY_NOTES, notesJson).apply();
    }

    public List<Note> loadNotes() {
        String notesJson = sharedPreferences.getString(KEY_NOTES, "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Note>>() {}.getType();
        return gson.fromJson(notesJson, type);
    }
}
