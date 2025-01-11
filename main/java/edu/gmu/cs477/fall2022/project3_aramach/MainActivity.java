package edu.gmu.cs477.fall2022.project3_aramach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cs-477-project-3-default-rtdb.firebaseio.com/");
        dbRef.child("player_specifics").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, HashMap<String, String>> result = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
                            if (!result.get("Y").get("Time").equals("")) {
                                Date d = new Date();
                                if (Math.abs(Long.parseLong(result.get("Y").get("Time")) - d.getTime()) > 600000) {
                                    dbRef.child("player_specifics").child("Y").child("Name").setValue("");
                                    dbRef.child("player_specifics").child("Y").child("Time").setValue("");
                                }
                            }
                            else {
                                dbRef.child("player_specifics").child("Y").child("Name").setValue("");
                                dbRef.child("player_specifics").child("Y").child("Time").setValue("");
                            }
                            if (!result.get("R").get("Time").equals("")) {
                                Date d = new Date();
                                if (Math.abs(Long.parseLong(result.get("R").get("Time")) - d.getTime()) > 600000) {
                                    dbRef.child("player_specifics").child("R").child("Name").setValue("");
                                    dbRef.child("player_specifics").child("R").child("Time").setValue("");
                                }
                            }
                            else {
                                dbRef.child("player_specifics").child("R").child("Name").setValue("");
                                dbRef.child("player_specifics").child("R").child("Time").setValue("");
                            }
                        }
                    }
                });
    }

    public void showInstructions(View view) {
        Intent intent = new Intent(this, Instructions.class);
        startActivity(intent);
    }

    public void playGame(View view) {
        Intent intent = new Intent(this, EnterPlayerNames.class);
        startActivity(intent);
    }
}