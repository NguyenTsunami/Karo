package com.example.karo.utility;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.karo.HomeActivity;
import com.example.karo.MainActivity;
import com.example.karo.R;
import com.example.karo.RoomActivity;
import com.example.karo.model.Cell;
import com.example.karo.model.Room;
import com.example.karo.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class CommonLogic {

    public static void gotoLoginScreen(Context context) {
        // intent to MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void handleSignIn(Context context, String email, String password, int modeLogin) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.COLLECTION_USERS)
                .whereEqualTo(Const.KEY_EMAIL, email)
                .whereEqualTo(Const.KEY_PASSWORD, password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && querySnapshot.size() > 0) {
                            Map<String, Object> map = querySnapshot.getDocuments().get(0).getData();
                            if (map != null) {
                                // get data of current user
                                User user = new User(
                                        Objects.requireNonNull(map.get(Const.KEY_EMAIL)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_PASSWORD)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_USERNAME)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_AVATAR_REF)).toString()
                                        , Integer.parseInt(Objects.requireNonNull(map.get(Const.KEY_SCORE)).toString())
                                );

                                // authenticate
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Sign in success, go to home screen
                                                gotoHomeScreen(context, user, querySnapshot.getDocuments().get(0).getId());
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                CommonLogic.makeToast(context, "Login fail!");
                                            }
                                        });
                            }
                        } else {
                            if (modeLogin == Const.MODE_LOGIN_FROM_INPUT) {
                                CommonLogic.makeToast(context, "Login fail!");
                            }
                        }
                    } else {
                        CommonLogic.makeToast(context, "Error: " + task.getException());
                    }
                });
    }

    public static void gotoHomeScreen(Context context, User user, String currentUserDocument) {
        // Save data login into cache
        SharedPreferences prefs = context.getSharedPreferences(Const.XML_NAME_CURRENT_USER, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Const.KEY_EMAIL, user.getEmail());
        editor.putString(Const.KEY_PASSWORD, user.getPassword());
        editor.putString(Const.KEY_USERNAME, user.getUsername());
        editor.putString(Const.KEY_AVATAR_REF, user.getAvatarRef());
        editor.putInt(Const.KEY_SCORE, user.getScore());
        editor.putString(Const.KEY_CURRENT_USER_DOCUMENT, currentUserDocument);
        editor.commit();

        // intent to HomeActivity
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void handleSignUp(Context context, String email, String password, boolean isAgreeWithTerms) {
        // Check validation
        if (!isEmailValid(email)) {
            CommonLogic.makeToast(context, "Please fill in a valid email address!");
            return;
        }
        if (password.equals("")) {
            CommonLogic.makeToast(context, "Please fill in password!");
            return;
        }
        if (!isAgreeWithTerms) {
            CommonLogic.makeToast(context, "Please agrees with terms before sign up an account!");
            return;
        }

        // Arrange data
        User user = new User(email, password, Const.DEFAULT_USERNAME, Const.DEFAULT_AVATAR_REF, Const.DEFAULT_SCORE);

        // Check existence
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.COLLECTION_USERS)
                .whereEqualTo(Const.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        int numberOfAccHasEmail = querySnapshot != null ? querySnapshot.size() : 0;
                        if (numberOfAccHasEmail > 0) {
                            CommonLogic.makeToast(context, "This email has already registered. Please choose another email");
                        } else {
                            signUpAccount(context, user);
                        }
                    } else {
                        CommonLogic.makeToast(context, "Error: " + task.getException());
                    }
                });
    }

    public static void signUpAccount(Context context, User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.COLLECTION_USERS).add(user)
                .addOnSuccessListener(documentReference -> {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                            .addOnCompleteListener((Activity) context, task -> {
                                if (task.isSuccessful()) {
                                    // Sign up success
                                    String currentUserDocument = documentReference.getId();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Whoohoo!");
                                    builder.setMessage("Registered successfully!");
                                    builder.setIcon(R.drawable.karo);
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Let's go",
                                            (dialog, which) -> CommonLogic.gotoHomeScreen(context, user, currentUserDocument));
                                    builder.show();
                                } else {
                                    // If sign up fails, display a message to the user.
                                    CommonLogic.makeToast(context, "Error: " + task.getException());
                                }
                            });
                })
                .addOnFailureListener(e -> CommonLogic.makeToast(context, "Error: " + e));
    }

    public static void saveImageToInternalStorage(Bitmap bitmapImage, Context context, String imgName) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        // path to /data/data/<your_app>/app_images
        File directory = contextWrapper.getDir(Const.DIRECTORY_IMAGES, MODE_PRIVATE);
        // Create imageFile
        File imgFile = new File(directory, imgName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imgFile);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                makeToast(context, "Error: " + e.getMessage());
            }
        }
    }

    public static Bitmap loadImageFromInternalStorage(String imagePath) {
        return BitmapFactory.decodeFile(imagePath);
    }

    public static void downloadAvatarList(Context context) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference listRef = storage.getReference().child(Const.KEY_AVATARS_STORAGE);
        listRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        // All the items under listRef.
                        item.getBytes(Const.MAX_DOWNLOAD_FILE_BYTE)
                                .addOnSuccessListener(bytes -> {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    CommonLogic.saveImageToInternalStorage(bitmap, context, item.getName());
                                })
                                .addOnFailureListener(e -> CommonLogic.makeToast(context, "Error: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    // Uh-oh, an error occurred!
                    CommonLogic.makeToast(context, "Error: " + e.getMessage());
                });
    }

    public static void deleteRoom(Context context, String roomDocument) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.COLLECTION_ROOMS).document(roomDocument)
                .delete()
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> makeToast(context, "Error: " + e.getMessage()));
    }

    public static void createRoom(Context context, String currentUserEmail) {
        // create new room
        Room room = new Room(currentUserEmail, null,
                Const.PLAYER_STATE_JOIN_ROOM, Const.PLAYER_STATE_NONE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.COLLECTION_ROOMS).add(room)
                .addOnSuccessListener(documentReference -> {
                    String roomDocument = documentReference.getId();
                    Intent intent = new Intent(context, RoomActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Const.KEY_ROOM_DOCUMENT, roomDocument);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> CommonLogic.makeToast(context, "Error: " + e));
    }

    public static void makeToast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

    public static int xPosition(int index) {
        return index / Const.COLUMN_SIZE;
    }

    public static int yPosition(int index) {
        return index % Const.COLUMN_SIZE;
    }

    public static int index(int i, int j) {
        return i * Const.COLUMN_SIZE + j;
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static boolean isFullBoardGame(ArrayList<Cell> cells) {
        for (int i = 0; i < Const.ROW_SIZE * Const.COLUMN_SIZE; i++) {
            if (cells.get(i).getToken().equals(Const.TOKEN_BLANK)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEndBoardGame(ArrayList<Cell> cells, int xpos, int ypos) {
        String token = cells.get(index(xpos, ypos)).getToken();
        String oppositeToken = token.equals(Const.TOKEN_X) ? Const.TOKEN_O : Const.TOKEN_X;
        int rows = Const.ROW_SIZE;
        int cols = Const.COLUMN_SIZE;

        //Check Vertical
        for (int i = max(0, xpos - 4); i <= min(xpos, rows - 5); i++) {
            boolean checkLine = true;
            for (int j = i; j < i + 5; j++) {
                if (!cells.get(index(j, ypos)).getToken().equals(token)) {
                    checkLine = false;
                    break;
                }
            }
            if (checkLine) {
                boolean check2Head = true;
                if (i - 1 >= 0 && cells.get(index(i - 1, ypos)).getToken().equals(oppositeToken)
                        && i + 5 < rows && cells.get(index(i + 5, ypos)).getToken().equals(oppositeToken)) {
                    check2Head = false;
                }
                if (check2Head) {
                    for (int j = i; j < i + 5; j++) {
                        cells.get(index(j, ypos)).setOnWinLine(true);
                    }
                    return true;
                }
            }
        }

        //Check Horizontal
        for (int i = max(0, ypos - 4); i <= min(ypos, cols - 5); i++) {
            boolean checkLine = true;
            for (int j = i; j < i + 5; j++) {
                if (!cells.get(index(xpos, j)).getToken().equals(token)) {
                    checkLine = false;
                    break;
                }
            }
            if (checkLine) {
                boolean check2Head = true;
                if (i - 1 >= 0 && cells.get(index(xpos, i - 1)).getToken().equals(oppositeToken)
                        && i + 5 < cols && cells.get(index(xpos, i + 5)).getToken().equals(oppositeToken)) {
                    check2Head = false;
                }
                if (check2Head) {
                    for (int j = i; j < i + 5; j++) {
                        cells.get(index(xpos, j)).setOnWinLine(true);
                    }
                    return true;
                }
            }
        }

        //Check Diagonal 1
        for (int i = max(0, xpos - 4); i <= min(xpos, rows - 5); i++) {
            boolean checkLine = true;
            for (int j = i; j < i + 5; j++) {
                int k = ypos - (j - xpos);
                if (k < 0 || k >= cols || !cells.get(index(j, k)).getToken().equals(token)) {
                    checkLine = false;
                    break;
                }
            }
            if (checkLine) {
                boolean check2Head = true;
                if (i - 1 >= 0 && ypos - (i - 1 - xpos) < cols
                        && cells.get(index(i - 1, ypos - (i - 1 - xpos))).getToken().equals(oppositeToken)
                        && i + 5 < rows && ypos - (i + 5 - xpos) >= 0
                        && cells.get(index(i + 5, ypos - (i + 5 - xpos))).getToken().equals(oppositeToken)) {
                    check2Head = false;
                }
                if (check2Head) {
                    for (int j = i; j < i + 5; j++) {
                        cells.get(index(j, ypos - (j - xpos))).setOnWinLine(true);
                    }
                    return true;
                }
            }
        }

        //Check Diagonal 2
        for (int i = max(0, xpos - 4); i <= min(xpos, rows - 5); i++) {
            boolean checkLine = true;
            for (int j = i; j < i + 5; j++) {
                int k = ypos + (j - xpos);
                if (k < 0 || k >= cols || !cells.get(index(j, k)).getToken().equals(token)) {
                    checkLine = false;
                    break;
                }
            }
            if (checkLine) {
                boolean check2Head = true;
                if (i - 1 >= 0 && ypos + (i - 1 - xpos) >= 0
                        && cells.get(index(i - 1, ypos + (i - 1 - xpos))).getToken().equals(oppositeToken)
                        && i + 5 < rows && ypos + (i + 5 - xpos) < cols
                        && cells.get(index(i + 5, ypos + (i + 5 - xpos))).getToken().equals(oppositeToken)) {
                    check2Head = false;
                }
                if (check2Head) {
                    for (int j = i; j < i + 5; j++) {
                        cells.get(index(j, ypos + (j - xpos))).setOnWinLine(true);
                    }
                    return true;
                }
            }
        }

        return false;
    }
}
