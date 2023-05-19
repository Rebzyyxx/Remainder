package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class NoteAdapter extends ArrayAdapter<Note> {


    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        Note note = getItem(position);

        TextView titleTextView = convertView.findViewById(android.R.id.text1);
        titleTextView.setText(note.getTitle());

        return convertView;

    }

}

