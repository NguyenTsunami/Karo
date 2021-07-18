package com.example.karo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.karo.model.User;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditProfileActivity extends AppCompatActivity {

    private User currentUser = null;
    private EditText txtCurrentEmail;
    private EditText txtCurrentUsername;
    private Button btnUploadPhoto;
    private Button btnSave;
    private ProgressBar progressBar;
    private ImageView imgCurrentAvatar;
    private boolean isChangeAvatar = false;
    private String currentUserDocument;
    private String avatarRefPicked;

    public Activity getActivity() {
        return this;
    }

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Get current user data
        SharedPreferences prefs = getSharedPreferences(Const.XML_NAME_CURRENT_USER, MODE_PRIVATE);
        String email = prefs.getString(Const.KEY_EMAIL, "");
        String password = prefs.getString(Const.KEY_PASSWORD, "");
        String username = prefs.getString(Const.KEY_USERNAME, "");
        String avatarRef = prefs.getString(Const.KEY_AVATAR_REF, "");
        int score = prefs.getInt(Const.KEY_SCORE, 0);
        currentUser = new User(email, password, username, avatarRef, score);
        currentUserDocument = prefs.getString(Const.KEY_CURRENT_USER_DOCUMENT, "");

        // get UI
        txtCurrentEmail = findViewById(R.id.txtCurrentEmail);
        txtCurrentEmail.setText(currentUser.getEmail());
        txtCurrentUsername = findViewById(R.id.txtCurrentUsername);
        txtCurrentUsername.setText(currentUser.getUsername());
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        imgCurrentAvatar = findViewById(R.id.imgCurrentAvatar);

        // disable email
        txtCurrentEmail.setEnabled(false);

        // hide progress bar
        progressBar.setVisibility(View.GONE);

        // load avatar
        Bitmap bitmap = CommonLogic.loadImageFromInternalStorage(
                Const.AVATARS_SOURCE_INTERNAL_PATH + currentUser.getAvatarRef());
        imgCurrentAvatar.setImageBitmap(bitmap);

        // set up button upload photo
        btnUploadPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, AvatarsActivity.class);
            startActivityForResult(intent, Const.REQUEST_CHANGE_AVATAR);
        });

        // set up button save
        btnSave.setOnClickListener(v -> {
            updateCurrentUser();
        });
    }

    private void updateCurrentUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentUserRef = db.collection(Const.COLLECTION_USERS).document(currentUserDocument);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // update to firebase cloud
            if (isChangeAvatar) {
                transaction.update(currentUserRef, Const.KEY_AVATAR_REF, avatarRefPicked);
            }
            transaction.update(currentUserRef, Const.KEY_USERNAME, txtCurrentUsername.getText().toString());
            // update cache
            SharedPreferences prefs = getSharedPreferences(Const.XML_NAME_CURRENT_USER, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (isChangeAvatar) {
                editor.putString(Const.KEY_AVATAR_REF, avatarRefPicked);
            }
            editor.putString(Const.KEY_USERNAME, txtCurrentUsername.getText().toString());
            editor.commit();
            return null;
        }).addOnSuccessListener(aVoid -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Got it!");
            builder.setMessage("Updated successfully!");
            builder.setIcon(R.drawable.karo);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", (dialog, which) -> {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
            });
            builder.show();
        }).addOnFailureListener(e -> CommonLogic.makeToast(this, "Transaction failure: " + e.getMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Const.REQUEST_CHANGE_AVATAR) {
                // get avatarRef that user picked
                String imgPickedRef = data.getStringExtra(Const.IMG_PICKED_REF);
                if (imgPickedRef != null && !imgPickedRef.equals(currentUser.getAvatarRef())) {
                    isChangeAvatar = true;
                    avatarRefPicked = imgPickedRef;
                    Bitmap bitmap = CommonLogic.loadImageFromInternalStorage(
                            Const.AVATARS_SOURCE_INTERNAL_PATH + avatarRefPicked);
                    imgCurrentAvatar.setImageBitmap(bitmap);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}