package com.example.karo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.karo.model.User;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Hide the navigation bar.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // Login
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    CommonLogic.gotoLoginScreen(getApplicationContext());
                } else {
                    SharedPreferences prefs = getSharedPreferences(Const.XML_NAME_CURRENT_USER, MODE_PRIVATE);
                    String email = prefs.getString(Const.KEY_EMAIL, "");
                    String password = prefs.getString(Const.KEY_PASSWORD, "");
                    CommonLogic.handleSignIn(getApplicationContext(), email, password, Const.MODE_LOGIN_FROM_CACHE);
                }
            }
        }, 1 * 1000); // wait for 1 seconds
    }
}