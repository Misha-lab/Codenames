package com.example.myapp.model;

import androidx.annotation.NonNull;

import com.example.myapp.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GameList {

    private DatabaseReference dr;
    private ArrayList<Integer> gameCodes;

    public GameList() {
        setUpdater();
    }

    private void setUpdater() {
        dr = FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference("game_list");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gameCodes = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds != null) {
                        gameCodes.add(ds.getValue(Integer.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference("game_list")
                .child("game0").setValue(1);

    }

    public ArrayList<Integer> getGameCodes() {
        return gameCodes;
    }
}
