package com.example.karo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.karo.adapter.GameBoardAdapter;
import com.example.karo.model.Cell;
import com.example.karo.model.Room;
import com.example.karo.model.User;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class RoomActivity extends AppCompatActivity {

    // UI
    private ImageView imgCurrentPlayerAvatar;
    private TextView txtCurrentPlayerName;
    private ImageView imgOpponentPlayerAvatar;
    private TextView txtOpponentPlayerName;
    private Button btnReady;
    private GridView gridviewBoardGame;
    private View viewOpponentInfo;
    private View viewInvitePlayers;
    private View viewWaitingState;
    private View viewReadyState;
    private View viewGameBoard;
    private ViewGroup layoutGameScreen;
    private ViewGroup layoutOpponentScreen;
    private ImageView imgYourTurn;
    private ImageView imgOpponentTurn;
    private ImageView imgCurrentPlayerRole;
    private ImageView imgOpponentPlayerRole;

    // Logic View
    private User currentUser;
    private User opponentUser;
    private int currentUserState;
    private int opponentUserState;
    private Room room;
    private String roomDocument;
    private String currentUserDocument;
    private ListenerRegistration roomListenerRegistration;
    private GameBoardAdapter gameBoardAdapter;

    // Logic game
    private String whoseTurn = Const.TOKEN_BLANK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // get view component
        layoutGameScreen = findViewById(R.id.layoutGameScreen);
        layoutOpponentScreen = findViewById(R.id.layoutOpponentScreen);
        viewOpponentInfo = getLayoutInflater().inflate(R.layout.component_opponent_info, null);
        viewInvitePlayers = getLayoutInflater().inflate(R.layout.component_invite_players, null);
        viewWaitingState = getLayoutInflater().inflate(R.layout.component_waiting_state, null);
        viewReadyState = getLayoutInflater().inflate(R.layout.component_ready_state, null);
        viewGameBoard = getLayoutInflater().inflate(R.layout.component_game_board, null);

        // get UI
        imgCurrentPlayerAvatar = findViewById(R.id.imgCurrentPlayerAvatar);
        txtCurrentPlayerName = findViewById(R.id.txtCurrentPlayerName);
        imgYourTurn = findViewById(R.id.imgYourTurn);
        imgOpponentTurn = viewOpponentInfo.findViewById(R.id.imgOpponentTurn);
        imgCurrentPlayerRole = findViewById(R.id.imgCurrentPlayerRole);
        imgOpponentPlayerRole = viewOpponentInfo.findViewById(R.id.imgOpponentPlayerRole);

        // gone turn at first time
        imgYourTurn.setVisibility(View.GONE);
        imgOpponentTurn.setVisibility(View.GONE);

        // set event btn ready
        btnReady = viewReadyState.findViewById(R.id.btnReady);
        btnReady.setOnClickListener(v -> {
            // send state 1 of current player to room
            sendState(Const.PLAYER_STATE_READY);
        });

        // get room id
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        roomDocument = bundle.getString(Const.KEY_ROOM_DOCUMENT);

        // set grid view adapter
        setupBoardGameAdapter();

        // load current player
        loadCurrentPlayerInfo();

        // load room
        loadRoomState();
    }

    private void setupBoardGameAdapter() {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = 0; i < Const.ROW_SIZE; i++) {
            for (int j = 0; j < Const.COLUMN_SIZE; j++) {
                cells.add(new Cell(Const.TOKEN_BLANK));
            }
        }
        gameBoardAdapter = new GameBoardAdapter(this, cells, roomDocument);
        gridviewBoardGame = viewGameBoard.findViewById(R.id.gridviewBoardGame);
        gridviewBoardGame.setAdapter(gameBoardAdapter);
    }

    private void loadCurrentPlayerInfo() {
        // Get current user data
        SharedPreferences prefs = getSharedPreferences(Const.XML_NAME_CURRENT_USER, MODE_PRIVATE);
        String email = prefs.getString(Const.KEY_EMAIL, "");
        String password = prefs.getString(Const.KEY_PASSWORD, "");
        String username = prefs.getString(Const.KEY_USERNAME, "");
        String avatarRef = prefs.getString(Const.KEY_AVATAR_REF, "");
        int score = prefs.getInt(Const.KEY_SCORE, 0);
        currentUser = new User(email, password, username, avatarRef, score);

        // load avatar
        Bitmap bitmap = CommonLogic.loadImageFromInternalStorage(
                Const.AVATARS_SOURCE_INTERNAL_PATH + currentUser.getAvatarRef());
        imgCurrentPlayerAvatar.setImageBitmap(bitmap);

        // upload username
        txtCurrentPlayerName.setText(currentUser.getUsername());

        // Get current user document
        currentUserDocument = prefs.getString(Const.KEY_CURRENT_USER_DOCUMENT, "");
    }

    private void sendState(int state) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference roomRef = db.collection(Const.COLLECTION_ROOMS).document(roomDocument);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // send state
            if (currentUser.getEmail().equals(room.getPlayerRoleXEmail())) {
                transaction.update(roomRef, Const.KEY_PLAYER_ROLE_X_STATE, state);
                if (state == Const.PLAYER_STATE_NONE) {
                    transaction.update(roomRef, Const.KEY_PLAYER_ROLE_X_EMAIL, null);
                }
            } else {
                transaction.update(roomRef, Const.KEY_PLAYER_ROLE_O_STATE, state);
                if (state == Const.PLAYER_STATE_NONE) {
                    transaction.update(roomRef, Const.KEY_PLAYER_ROLE_O_EMAIL, null);
                }
            }
            return null;
        }).addOnSuccessListener(aVoid -> {

        }).addOnFailureListener(e -> CommonLogic.makeToast(this, "Send state failure: " + e.getMessage()));
    }

    private void loadRoomState() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference roomDocRef = db.collection(Const.COLLECTION_ROOMS).document(roomDocument);
        roomListenerRegistration = roomDocRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                CommonLogic.makeToast(getApplicationContext(), error.getMessage());
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                // get new room
                Map<String, Object> map = snapshot.getData();
                String playerRoleXEmail = null;
                String playerRoleOEmail = null;
                if (map.get(Const.KEY_PLAYER_ROLE_X_EMAIL) != null) {
                    playerRoleXEmail = map.get(Const.KEY_PLAYER_ROLE_X_EMAIL).toString();
                }
                if (map.get(Const.KEY_PLAYER_ROLE_O_EMAIL) != null) {
                    playerRoleOEmail = map.get(Const.KEY_PLAYER_ROLE_O_EMAIL).toString();
                }
                Room newRoom = new Room(
                        playerRoleXEmail,
                        playerRoleOEmail,
                        Integer.parseInt(map.get(Const.KEY_PLAYER_ROLE_X_STATE).toString()),
                        Integer.parseInt(map.get(Const.KEY_PLAYER_ROLE_O_STATE).toString()),
                        Integer.parseInt(map.get(Const.KEY_PICK_CELL).toString())
                );

                // check state change
                if (room == null
                        || room.getPlayerRoleXState() != newRoom.getPlayerRoleXState()
                        || room.getPlayerRoleOState() != newRoom.getPlayerRoleOState()) {
                    handleNewRoomState(room, newRoom);
                }
                // check receive pick-cell
                else if (room.getPickCell() != newRoom.getPickCell()) {
                    handleReceivePickCell(newRoom.getPickCell());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        roomListenerRegistration.remove();
        super.onDestroy();
    }

    private void handleReceivePickCell(int pickCell) {
        room.setPickCell(pickCell);
        if (pickCell != -1) {
            // draw token to pickCell
            gameBoardAdapter.updateToken(pickCell, whoseTurn);
            // Check state of game
            int xPos = CommonLogic.xPosition(pickCell);
            int yPos = CommonLogic.yPosition(pickCell);
            if (CommonLogic.isEndBoardGame(gameBoardAdapter.getCells(), xPos, yPos)) {
                handleEndGame();
            } else if (CommonLogic.isFullBoardGame(gameBoardAdapter.getCells())) {
                handleDraw();
            } else {
                // flip turn
                whoseTurn = (whoseTurn.equals(Const.TOKEN_X) ? Const.TOKEN_O : Const.TOKEN_X);
            }
            // picked -> set disable
            gameBoardAdapter.setEnablePick(false);
            // set turn
            setTurn();
        }
    }

    private void handleDraw() {
        gameBoardAdapter.clearCells();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.karo);
        builder.setCancelable(false);
        builder.setTitle("Hmmm!");
        builder.setMessage("Game is draw!");
        builder.setPositiveButton("OK", (dialog, which) -> {
            // back to state 0
            sendState(Const.PLAYER_STATE_JOIN_ROOM);
        });
        builder.show();
    }

    private void handleEndGame() {
        gameBoardAdapter.clearCells();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.karo);
        builder.setCancelable(false);

        if (whoseTurn.equals(Const.TOKEN_X)
                && currentUser.getEmail().equals(room.getPlayerRoleXEmail())) {
            updateScore(Const.WIN_SCORE_EARN);
            builder.setTitle("Whoohoo!");
            builder.setMessage("You won!");
        } else if (whoseTurn.equals(Const.TOKEN_X)
                && currentUser.getEmail().equals(room.getPlayerRoleOEmail())) {
            builder.setTitle("Whoopss!");
            builder.setMessage("You lose!");
        } else if (whoseTurn.equals(Const.TOKEN_O)
                && currentUser.getEmail().equals(room.getPlayerRoleXEmail())) {
            builder.setTitle("Whoopss!");
            builder.setMessage("You lose!");
        } else if (whoseTurn.equals(Const.TOKEN_O)
                && currentUser.getEmail().equals(room.getPlayerRoleOEmail())) {
            updateScore(Const.WIN_SCORE_EARN);
            builder.setTitle("Whoohoo!");
            builder.setMessage("You won!");
        }
        builder.setPositiveButton("OK", (dialog, which) -> {
            // back to state 0
            sendState(Const.PLAYER_STATE_JOIN_ROOM);
        });
        builder.show();
    }

    private void updateScore(int extras) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentUserRef = db.collection(Const.COLLECTION_USERS).document(currentUserDocument);
        int newScore = currentUser.getScore() + extras;
        currentUser.setScore(newScore);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(currentUserRef, Const.KEY_SCORE, newScore);
            return null;
        }).addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e ->
                CommonLogic.makeToast(this, "Update score failure: " + e.getMessage()));
    }

    private void sendIndexNotifyNewBoardGame() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference roomRef = db.collection(Const.COLLECTION_ROOMS).document(roomDocument);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(roomRef, Const.KEY_PICK_CELL, -1);
            return null;
        }).addOnSuccessListener(aVoid -> setTurn())
                .addOnFailureListener(e ->
                        CommonLogic.makeToast(this, "Send index to out board game failure: " + e.getMessage()));
    }

    private void setTurn() {
        if (whoseTurn.equals(Const.TOKEN_X)
                && currentUser.getEmail().equals(room.getPlayerRoleXEmail())) {
            gameBoardAdapter.setEnablePick(true);
            imgYourTurn.setVisibility(View.VISIBLE);
            imgOpponentTurn.setVisibility(View.GONE);
        } else if (whoseTurn.equals(Const.TOKEN_X)
                && currentUser.getEmail().equals(room.getPlayerRoleOEmail())) {
            gameBoardAdapter.setEnablePick(false);
            imgYourTurn.setVisibility(View.GONE);
            imgOpponentTurn.setVisibility(View.VISIBLE);
        } else if (whoseTurn.equals(Const.TOKEN_O)
                && currentUser.getEmail().equals(room.getPlayerRoleXEmail())) {
            gameBoardAdapter.setEnablePick(false);
            imgYourTurn.setVisibility(View.GONE);
            imgOpponentTurn.setVisibility(View.VISIBLE);
        } else if (whoseTurn.equals(Const.TOKEN_O)
                && currentUser.getEmail().equals(room.getPlayerRoleOEmail())) {
            gameBoardAdapter.setEnablePick(true);
            imgYourTurn.setVisibility(View.VISIBLE);
            imgOpponentTurn.setVisibility(View.GONE);
        }
    }

    private void setRoleImageForPlayers() {
        // set role image for players
        if (currentUser.getEmail().equals(room.getPlayerRoleXEmail())) {
            imgCurrentPlayerRole.setImageResource(R.drawable.icon_x);
            imgOpponentPlayerRole.setImageResource(R.drawable.icon_o);
        } else {
            imgCurrentPlayerRole.setImageResource(R.drawable.icon_o);
            imgOpponentPlayerRole.setImageResource(R.drawable.icon_x);
        }
    }

    private void handleNewRoomState(Room oldRoom, Room newRoom) {
        // update room
        room = newRoom;
        // if first player is current user
        if (currentUser.getEmail().equals(newRoom.getPlayerRoleXEmail())) {
            // check room creation with existed opponent (null => 0&0)
            if (oldRoom == null
                    && newRoom.getPlayerRoleOState() == Const.PLAYER_STATE_JOIN_ROOM) {
                setRoleImageForPlayers();
                loadOpponentPlayerInfo(newRoom.getPlayerRoleOEmail());
            }
            // check opponent join room (0&-1 => 0&0)
            else if (oldRoom != null && oldRoom.getPlayerRoleOState() == Const.PLAYER_STATE_NONE
                    && newRoom.getPlayerRoleOState() == Const.PLAYER_STATE_JOIN_ROOM) {
                setRoleImageForPlayers();
                loadOpponentPlayerInfo(newRoom.getPlayerRoleOEmail());
            }
            // check state start game (0&1/1&0/0&0 => 1&1)
            else if (oldRoom != null
                    && (oldRoom.getPlayerRoleXState() == Const.PLAYER_STATE_JOIN_ROOM
                    || oldRoom.getPlayerRoleOState() == Const.PLAYER_STATE_JOIN_ROOM)
                    && (newRoom.getPlayerRoleXState() == Const.PLAYER_STATE_READY
                    && newRoom.getPlayerRoleOState() == Const.PLAYER_STATE_READY)) {
                whoseTurn = Const.TOKEN_X;
                gameBoardAdapter.clearCells();
                sendIndexNotifyNewBoardGame();
            }
            // check opponent out room (!null => 1&-1)
            else if (oldRoom != null
                    && newRoom.getPlayerRoleXState() == Const.PLAYER_STATE_READY
                    && newRoom.getPlayerRoleOState() == Const.PLAYER_STATE_NONE) {
                notifyOpponentOutRoom();
            }
            // change state of players
            currentUserState = newRoom.getPlayerRoleXState();
            opponentUserState = newRoom.getPlayerRoleOState();
        }
        // if second player is current user
        else if (currentUser.getEmail().equals(newRoom.getPlayerRoleOEmail())) {
            // check room creation with existed opponent (null => 0&0)
            if (oldRoom == null
                    && newRoom.getPlayerRoleXState() == Const.PLAYER_STATE_JOIN_ROOM) {
                setRoleImageForPlayers();
                loadOpponentPlayerInfo(newRoom.getPlayerRoleXEmail());
            }
            // check opponent join room (0&-1 => 0&0)
            else if (oldRoom != null && oldRoom.getPlayerRoleXState() == Const.PLAYER_STATE_NONE
                    && newRoom.getPlayerRoleXState() == Const.PLAYER_STATE_JOIN_ROOM) {
                setRoleImageForPlayers();
                loadOpponentPlayerInfo(newRoom.getPlayerRoleXEmail());
            }
            // check state start game (0&1/1&0/0&0 => 1&1)
            else if (oldRoom != null
                    && (oldRoom.getPlayerRoleXState() == Const.PLAYER_STATE_JOIN_ROOM
                    || oldRoom.getPlayerRoleOState() == Const.PLAYER_STATE_JOIN_ROOM)
                    && (newRoom.getPlayerRoleXState() == Const.PLAYER_STATE_READY
                    && newRoom.getPlayerRoleOState() == Const.PLAYER_STATE_READY)) {
                whoseTurn = Const.TOKEN_X;
                gameBoardAdapter.clearCells();
                sendIndexNotifyNewBoardGame();
            }
            // check opponent out room (!null => 1&-1)
            else if (oldRoom != null
                    && newRoom.getPlayerRoleOState() == Const.PLAYER_STATE_READY
                    && newRoom.getPlayerRoleXState() == Const.PLAYER_STATE_NONE) {
                notifyOpponentOutRoom();
            }
            // change state of players
            currentUserState = newRoom.getPlayerRoleOState();
            opponentUserState = newRoom.getPlayerRoleXState();
        }
        // inflate component view
        inflateLayout();
    }

    private void notifyOpponentOutRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.karo);
        builder.setCancelable(false);
        builder.setTitle("Oops!");
        builder.setMessage("Opponent left room :((");
        builder.setPositiveButton("OK", (dialog, which) -> {
            // back to state 0
            sendState(Const.PLAYER_STATE_JOIN_ROOM);
        });
        builder.show();
    }

    private void loadOpponentPlayerInfo(String opponentEmail) {
        // remove old data
        imgOpponentPlayerAvatar = viewOpponentInfo.findViewById(R.id.imgOpponentPlayerAvatar);
        txtOpponentPlayerName = viewOpponentInfo.findViewById(R.id.txtOpponentPlayerName);
        imgOpponentPlayerAvatar.setVisibility(View.INVISIBLE);
        txtOpponentPlayerName.setVisibility(View.INVISIBLE);
        // get new data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.COLLECTION_USERS)
                .whereEqualTo(Const.KEY_EMAIL, opponentEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && querySnapshot.size() > 0) {
                            Map<String, Object> map = querySnapshot.getDocuments().get(0).getData();
                            if (map != null) {
                                opponentUser = new User(
                                        Objects.requireNonNull(map.get(Const.KEY_EMAIL)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_PASSWORD)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_USERNAME)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_AVATAR_REF)).toString()
                                        , Integer.parseInt(Objects.requireNonNull(map.get(Const.KEY_SCORE)).toString())
                                );
                                // load UI into view opponent info
                                Bitmap bitmap = CommonLogic.loadImageFromInternalStorage(
                                        Const.AVATARS_SOURCE_INTERNAL_PATH + opponentUser.getAvatarRef());
                                imgOpponentPlayerAvatar.setImageBitmap(bitmap);
                                txtOpponentPlayerName.setText(opponentUser.getUsername());
                                imgOpponentPlayerAvatar.setVisibility(View.VISIBLE);
                                txtOpponentPlayerName.setVisibility(View.VISIBLE);
                            }
                        } else {
                            CommonLogic.makeToast(this, "Error: Null user");
                        }
                    } else {
                        CommonLogic.makeToast(this, "Error: " + task.getException());
                    }
                });
    }

    private void inflateLayout() {
        // current user has just created room,
        // or current user has just joined room
        if (currentUserState == Const.PLAYER_STATE_JOIN_ROOM) {
            // no opponent yet
            if (opponentUserState == Const.PLAYER_STATE_NONE) {
                // show waiting screen
                layoutGameScreen.removeAllViews();
                layoutGameScreen.addView(viewWaitingState);
                // show invitation screen
                layoutOpponentScreen.removeAllViews();
                layoutOpponentScreen.addView(viewInvitePlayers);
            }
            // has opponent
            else if (opponentUserState == Const.PLAYER_STATE_JOIN_ROOM
                    || opponentUserState == Const.PLAYER_STATE_READY) {
                // show ready screen
                layoutGameScreen.removeAllViews();
                layoutGameScreen.addView(viewReadyState);
                // show opponent info screen
                layoutOpponentScreen.removeAllViews();
                layoutOpponentScreen.addView(viewOpponentInfo);
            }
        }
        // current user click ready
        else if (currentUserState == Const.PLAYER_STATE_READY) {
            // opponent not click ready yet
            if (opponentUserState == Const.PLAYER_STATE_JOIN_ROOM) {
                // show waiting screen
                layoutGameScreen.removeAllViews();
                layoutGameScreen.addView(viewWaitingState);
                // show opponent info screen
                layoutOpponentScreen.removeAllViews();
                layoutOpponentScreen.addView(viewOpponentInfo);
            }
            // opponent click ready
            else if (opponentUserState == Const.PLAYER_STATE_READY) {
                // show waiting screen
                layoutGameScreen.removeAllViews();
                layoutGameScreen.addView(viewGameBoard);
                // show opponent info screen
                layoutOpponentScreen.removeAllViews();
                layoutOpponentScreen.addView(viewOpponentInfo);
            }
        }
    }

    @Override
    public void onBackPressed() {
        confirmOutRoom();
    }

    private void confirmOutRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.karo);
        builder.setTitle("Wait!");
        builder.setMessage("Do you really want to left room???");
        builder.setNegativeButton("No", (dialog, which) -> {

        });
        builder.setPositiveButton("Yes", (dialog, which) -> handleOutRoom());
        builder.show();
    }

    private void handleOutRoom() {
        // if room have only one player (current user)
        if (opponentUserState == Const.PLAYER_STATE_NONE) {
            // stop listen
            roomListenerRegistration.remove();
            // delete room
            CommonLogic.deleteRoom(this, roomDocument);
        }
        // if opponent in room
        else {
            // back to state -1
            sendState(Const.PLAYER_STATE_NONE);
        }
        // come back to home screen
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}