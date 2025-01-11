package edu.gmu.cs477.fall2022.project3_aramach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class EnterPlayerNames extends AppCompatActivity {
    private EditText enter_name;
    private Button start_button;
    private DatabaseReference dbRef;

    public static String player_piece;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_player_names);
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cs-477-project-3-default-rtdb.firebaseio.com/");
        enter_name = findViewById(R.id.enter_name);
        start_button = findViewById(R.id.start_game);
        player_piece = "";

        dbRef.child("player_specifics").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dbRef.child("player_specifics").get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                System.out.println("About to try to start activity");
                                HashMap<String, HashMap<String, String>> result = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
                                if (!result.get("Y").get("Name").equals("") && !result.get("R").get("Name").equals("")) {
                                    System.out.println("Starting activity");
                                    Intent intent = new Intent(getApplicationContext(), PlayGame.class);
                                    startActivity(intent);
                                }
                            }
                        }
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        System.out.println("HERE 3!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        cleanup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RESUMING!!!!!!!!!!!!!!!!!!!!");
        if (!player_piece.equals("")) {
            cleanup();
        }
    }

    private void cleanup() {
        // Reset the Player 1 and Player 2 names
        System.out.println("HERE 4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        dbRef.child("player_specifics").child("Y").child("Name").setValue("");
        dbRef.child("player_specifics").child("R").child("Name").setValue("");
        finish();
    }

    public void startGame(View view) {
        String player_name = enter_name.getText().toString();
        start_button.setText("Waiting for Opponent...");
        start_button.setTextSize(15);
        start_button.setEnabled(false);
        dbRef.child("player_specifics").get()
            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, HashMap<String, String>> result = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
                        if (result.get("R").get("Name").equals("")) {
                            System.out.println("here1");
                            player_piece = "R";
                        }
                        else if (result.get("Y").get("Name").equals("")) {
                            System.out.println("here2");
                            player_piece = "Y";
                        }

                        if (player_piece.equals("")) {
                            cleanup();
                        }
                        else  {
                            System.out.println("here4");
                            dbRef.child("player_specifics").child(player_piece).child("Name").setValue(player_name)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dbRef.child("player_specifics").child(player_piece).child("Time").setValue(Long.toString((new Date()).getTime()));
                                        System.out.println(String.format("%s set as Player %s's name", player_name, player_piece));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("FAILED!!");
                                    }
                                });
                        }
                    }
                }
            });
    }
}