package edu.gmu.cs477.fall2022.project3_aramach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayGame extends AppCompatActivity {

    private final int NUM_COLS = 7;
    private final int NUM_ROWS = 6;
    private DatabaseReference dbRef;
    private TextView turn_label;
    private String my_name;
    private String opponent_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);
        turn_label = findViewById(R.id.turn_label);
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cs-477-project-3-default-rtdb.firebaseio.com/");

        dbRef.child("game_play_specifics").child("turn").setValue("R")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Turn initialized!!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed to initialize turn!!");
                    }
                });

        dbRef.child("game_play_specifics").child("most_recent_board_change").child("image_id").setValue("");
        dbRef.child("game_play_specifics").child("most_recent_board_change").child("player_committed").setValue("");

        dbRef.child("player_specifics").get()
            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, HashMap<String, String>> result = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
                        my_name = result.get(EnterPlayerNames.player_piece).get("Name");
                        opponent_name = result.get(EnterPlayerNames.player_piece.equals("Y") ? "R" : "Y").get("Name");
                    }
                }
            });

        for (int i = 0; i < NUM_COLS; i++) {
            List<String> column_list = new ArrayList<>();
            for (int j = 0; j < NUM_ROWS; j++) {
                column_list.add("null");
            }
            dbRef.child("game_board").child(Integer.toString(i)).setValue(column_list.toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("New game board added!!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed to add new game board: " + e);
                    }
                });
        }

        dbRef.child("game_play_specifics").child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String turn = (String) snapshot.getValue();
                dbRef.child("player_specifics").get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful());
                            HashMap<String, HashMap<String, String>> result = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
                            turn_label.setText("Turn: " + result.get(turn).get("Name"));
                        }
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });

        dbRef.child("game_board").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dbRef.child("game_play_specifics").child("most_recent_board_change").get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                HashMap<String, String> result = (HashMap<String, String>) task.getResult().getValue();
                                if (result.get("image_id").length() > 0) {
                                    ImageView img = getImage(result.get("image_id"));
                                    img.setImageResource(result.get("player_committed").equals("Y") ? R.drawable.yellow_game_piece : R.drawable.red_game_piece);

                                    ArrayList<ArrayList<String>> gb = new ArrayList<>();
                                    dbRef.child("game_board").get()
                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    ArrayList<String> game_board = (ArrayList<String>) task.getResult().getValue();
                                                    for (int j = 0; j < game_board.size(); j++) {
                                                        ArrayList<String> column_list = strToList(game_board.get(j));
                                                        gb.add(column_list);
                                                    }
                                                    int row = Character.getNumericValue(result.get("image_id").charAt(1));
                                                    int column = Character.getNumericValue(result.get("image_id").charAt(3));

                                                    checkWinner(gb, column, row, result.get("player_committed"));
                                                    String next_turn = result.get("player_committed").equals("Y") ? "R" : "Y";
                                                    dbRef.child("game_play_specifics").child("turn").setValue(next_turn);
//                                                    dbRef.child("player_specifics").get()
//                                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                                                if (task.isSuccessful()) {
//                                                                    HashMap<String, HashMap<String, String>> names = (HashMap<String, HashMap<String, String>>) task.getResult().getValue();
//                                                                    turn_label.setText(String.format("Turn: %s", names.get(next_turn).get("Name")));
//                                                                }
//                                                            }
//                                                        });
                                                }
                                            }
                                        });
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

    public void placePiece(View view) {
        dbRef.child("game_play_specifics").child("turn").get()
            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        String turn = task.getResult().getValue().toString();
                        if (turn.equals(EnterPlayerNames.player_piece)) {
                            String button_id = getStringID(view.getId());
                            int column = Character.getNumericValue(button_id.charAt(1));
                            // Get the column from the database
                            dbRef.child("game_board").child(Integer.toString(column)).get()
                                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            String result = task.getResult().getValue().toString();
                                            ArrayList<String> column_list = strToList(result);
                                            if (column_list.get(column_list.size() - 1).equals("null")) {
                                                int row = getIndex(column_list);
                                                dbRef.child("game_play_specifics").child("most_recent_board_change").child("image_id").setValue(String.format("R%dC%d", row, column));
                                                dbRef.child("game_play_specifics").child("most_recent_board_change").child("player_committed").setValue(turn);
                                                column_list.set(row, turn);
                                                dbRef.child("game_board").child(Integer.toString(column)).setValue(column_list.toString());
                                            }
                                        }
                                        else {
                                            System.out.println("ERROR: " + task.getException());
                                        }
                                    }
                                });
                        }
                    }
                }
            });
    }

    private void cleanup() {
        // Reset the Player 1 and Player 2 names
        System.out.println("HERE 2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        dbRef.child("player_specifics").child("Y").child("Name").setValue("");
        dbRef.child("player_specifics").child("R").child("Name").setValue("");
        // Reset the game play specifics
        dbRef.child("game_play_specifics").child("most_recent_board_change").child("image_id").setValue("");
        dbRef.child("game_play_specifics").child("most_recent_board_change").child("player_committed").setValue("");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("HERE 1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        cleanup();
    }

    private void checkWinner(ArrayList<ArrayList<String>> game_board, int column, int row, String turn) {
        // Tracking the number of pieces in a row found for the player in question
        int numConsecutive;
        // Get the list associated with the column in question
        ArrayList<String> column_list = (ArrayList<String>) game_board.get(column);

        /*
          Let us first check the 3 pieces below where the current piece was just placed
          (staying within the same column, moving between rows)
         */
        numConsecutive = 0;
        for (int i = row; i >= 0; i--) {
            if (column_list.get(i).equals(turn)) {
                numConsecutive++;
                if (numConsecutive >= 4) {
                    alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
                    return;
                }
            }
            else {
                numConsecutive = 0;
            }
        }
        if (numConsecutive >= 4) {
            alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
            return;
        }

        /*
         Let us now check the row of the current piece
         (staying within the same row, moving between columns)
         */
        numConsecutive = 0;
        for (int j = 0; j < NUM_COLS; j++) {
            ArrayList<String> col = game_board.get(j);
            if (col == null) {
                return;
            }
            if (turn.equals(col.get(row))) {
                numConsecutive++;
                if (numConsecutive >= 4) {
                    alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
                    return;
                }
            }
            else {
                numConsecutive = 0;
            }
        }
        if (numConsecutive >= 4) {
            alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
            return;
        }

        /*
         Let us now check the top left to bottom right diagonal corresponding to the current piece
         (moving between rows and columns each time)
         */
        numConsecutive = 0;
        // Going towards top left
        for (int i = row, j = column; i < NUM_ROWS && j >= 0; i++, j--) {
            ArrayList<String> col = game_board.get(j);
            if (turn.equals(col.get(i))) {
                numConsecutive++;
            }
            else {
                break;
            }
        }
        if (numConsecutive >= 4) {
            alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
            return;
        }
        // Going towards bottom right
        for (int i = row - 1, j = column + 1; i >= 0 && j < NUM_COLS; i--, j++) {
            ArrayList<String> col = game_board.get(j);
            if (turn.equals(col.get(i))) {
                numConsecutive++;
            }
            else {
                break;
            }
        }
        if (numConsecutive >= 4) {
            alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
            return;
        }

        /*
         Let us now check the bottom left to top right diagonal corresponding to the current piece
         (moving between rows and columns each time)
         */
        numConsecutive = 0;
        // Going towards bottom left
        for (int i = row, j = column; i >= 0 && j >= 0; i--, j--) {
            ArrayList<String> col = game_board.get(j);
            if (turn.equals(col.get(i))) {
                numConsecutive++;
            }
            else {
                break;
            }
        }
        if (numConsecutive >= 4) {
            alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
            return;
        }
        // Going towards top right
        for (int i = row + 1, j = column + 1; i < NUM_ROWS && j < NUM_COLS; i++, j++) {
            ArrayList<String> col = game_board.get(j);
            if (turn.equals(col.get(i))) {
                numConsecutive++;
            }
            else {
                break;
            }
        }
        if (numConsecutive >= 4) {
            alert((turn.equals(EnterPlayerNames.player_piece) ? my_name : opponent_name) + " has won!");
            return;
        }

        // Check for a draw
        int num_full_cols = 0;
        for (int i = 0; i < NUM_COLS; i++) {
            if (!game_board.get(i).get(NUM_ROWS - 1).equals("null")) {
                num_full_cols++;
            }
        }
        if (num_full_cols == NUM_COLS) {
            alert("It's a Draw!");
        }
    }

    private String getStringID(int id) {
        return getResources().getResourceEntryName(id);
    }

    private ImageView getImage(String id) {
        return findViewById(getResources().getIdentifier(id, "id", getPackageName()));
    }

    private ArrayList<String> strToList(String strList) {
        // Get rid of the first and last chars in the strList (the opening and closing brackets
        strList = strList.substring(1, strList.length() - 1);
        String[] arr = strList.split(",");
        ArrayList<String> res = new ArrayList<>();
        for (String s : arr) {
            res.add(s.trim());
        }
        return res;
    }

    private int getIndex(ArrayList<String> list) {
        int i;
        for (i = 0; i < list.size(); i++) {
            if (list.get(i).equals("null")) {
                break;
            }
        }
        return i;
    }

    private void alert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(message).setIcon(R.drawable.winning).setMessage("Click to Return to Home")
                .setPositiveButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cleanup();
                    }
                })
                .show();
    }

}