package com.example.myapplication;

import static com.example.myapplication.FBRef.refAuth;
import static com.example.myapplication.FBRef.refPlayer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText eTEmail;
    private EditText eTPass;
    private EditText username;
    private Button signUp;
    private TextView gotoSignin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        eTEmail = findViewById(R.id.email);
        eTPass = findViewById(R.id.password);
        username = findViewById(R.id.username);
        signUp = findViewById(R.id.signup_button);
        gotoSignin = findViewById(R.id.goto_signin);
        signUp.setOnClickListener(this);
        gotoSignin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == signUp.getId()) {
            String email = eTEmail.getText().toString();
            String pass = eTPass.getText().toString();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                ProgressDialog pd = new ProgressDialog(this);
                pd.setTitle("Connecting");
                pd.setMessage("Creating user...");
                pd.show();
                refAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                pd.dismiss();
                                if (task.isSuccessful()) {
                                    Log.i("MainActivity", "createUserWithEmailAndPassword:success");
                                    FirebaseUser user = refAuth.getCurrentUser();

                                    String displayName = username.getText().toString();
                                    Player new_player = new Player(displayName);

                                    String UserID = user.getUid();
                                    new_player.setPlayerID(UserID);
                                    refPlayer.child(UserID).setValue(new_player.getMap());

                                    Intent intent = new Intent(SignupActivity.this, GameMenuActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Exception exp = task.getException();
                                    if (exp instanceof FirebaseAuthInvalidUserException) {
                                        Toast.makeText(SignupActivity.this, "Invalid email address.", Toast.LENGTH_SHORT).show();
                                    } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                        Toast.makeText(SignupActivity.this, "Password too weak. Use at least 6 chars", Toast.LENGTH_SHORT).show();
                                    } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(SignupActivity.this, "Email already in use.", Toast.LENGTH_SHORT).show();
                                    } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                        Toast.makeText(SignupActivity.this, "General authentication failure.", Toast.LENGTH_SHORT).show();
                                    } else if (exp instanceof FirebaseNetworkException) {
                                        Toast.makeText(SignupActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        } else {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        }

    }
}