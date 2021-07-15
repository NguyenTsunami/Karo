package com.example.karo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.karo.model.User;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Button btnSignIn, btnSignUp;
    private TextView txtSignUpLink, txtSignInLink;
    private ConstraintLayout layoutLoginModule;
    private Scene signInScene, signUpScene;
    private Context context = this;
    private String currentUserDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.activity_main);

        // Hide action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Change color of status bar
        Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

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
        btnSignUp.setOnClickListener(v -> handleSignUp());

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

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void handleSignUp() {
        // Get data
        EditText txtEmail = findViewById(R.id.txtEmail);
        EditText txtPass = findViewById(R.id.txtPass);
        String email = txtEmail.getText().toString();
        String password = txtPass.getText().toString();
        CheckBox ckbTermsCheck = findViewById(R.id.ckbTermsCheck);

        // Check validation
        if (!isEmailValid(email)) {
            CommonLogic.makeToast(this, "Please fill in a valid email address!");
            return;
        }
        if (password.equals("")) {
            CommonLogic.makeToast(this, "Please fill in password!");
            return;
        }
        if (!ckbTermsCheck.isChecked()) {
            CommonLogic.makeToast(this, "Please agrees with terms before sign up an account!");
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
                            CommonLogic.makeToast(this, "This email has already registered. Please choose another email");
                        } else {
                            signUpAccount(user);
                        }
                    } else {
                        CommonLogic.makeToast(this, "Error: " + task.getException());
                    }
                });
    }

    private void signUpAccount(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.COLLECTION_USERS).add(user)
                .addOnSuccessListener(documentReference -> {
                    currentUserDocument = documentReference.getId();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Whoohoo!");
                    builder.setMessage("Registered successfully!");
                    builder.setIcon(R.drawable.karo);
                    builder.setCancelable(false);
                    builder.setPositiveButton("Let's go", (dialog, which) -> CommonLogic.goHome(this, user, currentUserDocument));
                    builder.show();
                })
                .addOnFailureListener(e -> CommonLogic.makeToast(this, "Error: " + e));
    }
}