package com.example.notesapp;

import androidx.cardview.widget.CardView;

import com.example.notesapp.Models.Notes;

public interface NotesClick {
    void onClick(Notes notes, int position);

    void onLongClick(Notes notes, CardView cardView, int position);
}
