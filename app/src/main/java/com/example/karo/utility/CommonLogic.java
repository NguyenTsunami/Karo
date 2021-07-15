package com.example.karo.utility;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.example.karo.HomeActivity;
import com.example.karo.MainActivity;
import com.example.karo.model.Cell;
import com.example.karo.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

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
                                User user = new User(
                                        Objects.requireNonNull(map.get(Const.KEY_EMAIL)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_PASSWORD)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_USERNAME)).toString()
                                        , Objects.requireNonNull(map.get(Const.KEY_AVATAR_REF)).toString()
                                        , Integer.parseInt(Objects.requireNonNull(map.get(Const.KEY_SCORE)).toString())
                                );
                                goHome(context, user, querySnapshot.getDocuments().get(0).getId());
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

    public static void goHome(Context context, User user, String currentUserDocument) {
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

        // get avatar from cloud storage
        StorageReference rootRef = FirebaseStorage.getInstance().getReference();
        StorageReference imgStorageRef = rootRef.child(user.getAvatarRef());
        imgStorageRef.getBytes(Const.MAX_DOWNLOAD_FILE_BYTE)
                .addOnSuccessListener(bytes -> {
                    Bitmap avatarBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    editor.putString(Const.KEY_CURRENT_USER_AVATAR_PATH,
                            CommonLogic.saveImageToInternalStorage
                                    (avatarBitmap, context.getApplicationContext(), Const.CURRENT_USER_AVATAR_FILE_NAME));
                    editor.commit();
                })
                .addOnFailureListener(e -> {
                    CommonLogic.makeToast(context, "Error: " + e.getMessage());
                });

        // intent to HomeActivity
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String saveImageToInternalStorage(Bitmap bitmapImage, Context context, String imgName) {
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
        return imgFile.getAbsolutePath();
    }

    public static Bitmap loadImageFromInternalStorage(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        return bitmap;
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
        return (a > b) ? a : b;
    }

    public static int min(int a, int b) {
        return (a < b) ? a : b;
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
