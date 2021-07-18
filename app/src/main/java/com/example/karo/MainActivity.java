package com.example.karo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.karo.model.User;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Button btnSignIn, btnSignUp;
    private TextView txtSignUpLink, txtSignInLink;
    private ConstraintLayout layoutLoginModule;
    private Scene signInScene, signUpScene;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.activity_main);

        // Hide action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Change color of status bar and nav bar
        Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));

        // Setup first scene
        layoutLoginModule = findViewById(R.id.layoutLoginModule);
        signInScene = Scene.getSceneForLayout(layoutLoginModule, R.layout.activity_signin, this);
        signUpScene = Scene.getSceneForLayout(layoutLoginModule, R.layout.activity_signup, this);
        signInScene.enter();
        setupSignInView();

        // Setup Slide Animation
        setupSlideAnimation();
    }

    private void setupSignUpView() {
        // Handle Sign Up
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> {
            EditText txtEmail = findViewById(R.id.txtEmail);
            EditText txtPass = findViewById(R.id.txtPass);
            CheckBox ckbTermsCheck = findViewById(R.id.ckbTermsCheck);
            String email = txtEmail.getText().toString();
            String password = txtPass.getText().toString();
            boolean isAgreeWithTerms = ckbTermsCheck.isChecked();
            CommonLogic.handleSignUp(this, email, password, isAgreeWithTerms);
        });

        // Change Sign In Scene
        txtSignInLink = findViewById(R.id.txtSignInLink);
        txtSignInLink.setOnClickListener(v -> moveToSignInView());
    }

    private void setupSignInView() {
        // Handle Sign In
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(v -> {
            EditText txtEmail = findViewById(R.id.txtEmail);
            EditText txtPass = findViewById(R.id.txtPass);
            String email = txtEmail.getText().toString();
            String password = txtPass.getText().toString();
            CommonLogic.handleSignIn(this, email, password, Const.MODE_LOGIN_FROM_INPUT);
        });

        // Change Sign Up Scene
        txtSignUpLink = findViewById(R.id.txtSignUpLink);
        txtSignUpLink.setOnClickListener(v -> moveToSignUpView());
    }

    @SuppressLint("RtlHardcoded")
    private void moveToSignUpView() {
        Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.slide);
        slide.setSlideEdge(Gravity.LEFT);
        TransitionManager.go(signUpScene, slide);

        //Setup Sign Up View
        setupSignUpView();
    }

    @SuppressLint("RtlHardcoded")
    private void moveToSignInView() {
        Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.slide);
        slide.setSlideEdge(Gravity.RIGHT);
        TransitionManager.go(signInScene, slide);

        // Setup Sign In View
        setupSignInView();
    }

    @SuppressLint("RtlHardcoded")
    public void setupSlideAnimation() {
        Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.slide);
        slide.setSlideEdge(Gravity.LEFT);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }

}