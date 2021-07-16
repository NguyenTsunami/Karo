package com.example.karo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.karo.model.User;
import com.example.karo.utility.Const;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private User currentUser = null;
    private String currentUserDocument;

    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get current user data
        SharedPreferences prefs = getSharedPreferences(Const.XML_NAME_CURRENT_USER, MODE_PRIVATE);
        String email = prefs.getString(Const.KEY_EMAIL, "");
        String password = prefs.getString(Const.KEY_PASSWORD, "");
        String username = prefs.getString(Const.KEY_USERNAME, "");
        String avatarRef = prefs.getString(Const.KEY_AVATAR_REF, "");
        int score = prefs.getInt(Const.KEY_SCORE, 0);
        currentUser = new User(email, password, username, avatarRef, score);
        currentUserDocument = prefs.getString(Const.KEY_CURRENT_USER_DOCUMENT, "");

        // setup permission
        setPermission();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_ranks, R.id.navigation_rooms, R.id.navigation_invitations)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void setPermission() {
        // Check permission
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        if (!checkPermissions(permissions)) {
            requestPermissions(permissions);
        }

    }

    private boolean checkPermissions(String... permissions) {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, Const.PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Const.PERMISSION_REQUEST_CODE) {
            String result = "";
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    result += permissions[i] + " denied!\n";
                }
            }
            if (!result.equals("")) {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itemEditProfile:
                goToEditProfileScreen();
                break;
            case R.id.itemSignOut:
                confirmSignOut();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToEditProfileScreen() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        confirmSignOut();
    }

    private void handleSignOut() {
        // un-authenticate
        FirebaseAuth.getInstance().signOut();
        // remove from cache
        SharedPreferences prefs = getSharedPreferences(Const.XML_NAME_CURRENT_USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
        // intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void confirmSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hold on!");
        builder.setMessage("Are you really want to sign out?");
        builder.setIcon(R.drawable.karo);
        builder.setPositiveButton("Yes", (dialog, which) -> handleSignOut());
        builder.setNegativeButton("No", (dialog, which) -> {
        });
        builder.show();
    }

}