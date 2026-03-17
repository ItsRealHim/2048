package com.example.myapplication;

import static com.example.myapplication.FBRef.refAuth;
import static com.example.myapplication.FBRef.refPlayer;

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

public class SigninActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tVMsg;
    private EditText eTPass;
    private EditText eTEmail;
    private Button signIn;
    private TextView gotoSignUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        eTEmail = findViewById(R.id.email);
        eTPass = findViewById(R.id.password);
        signIn = findViewById(R.id.signin_button);
        gotoSignUp = findViewById(R.id.goto_signup);

        gotoSignUp.setOnClickListener(this);
        signIn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == signIn.getId()) {
            String email = eTEmail.getText().toString();
            String pass = eTPass.getText().toString();
            if (email.isEmpty() || pass.isEmpty()) {
                tVMsg.setText("Please fill all fields");
            } else {
                ProgressDialog pd = new ProgressDialog(this);
                pd.setTitle("Connecting");
                pd.setMessage("Logging in user...");
                pd.show();
                refAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Log.i("MainActivity", "createUserWithEmailAndPassword:success");
                            Intent intent = new Intent(SigninActivity.this, GameMenuActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthInvalidUserException) {
                                Toast.makeText(SigninActivity.this, "Invalid email address.", Toast.LENGTH_SHORT).show();
                            } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                Toast.makeText(SigninActivity.this, "Password too weak.", Toast.LENGTH_SHORT).show();
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(SigninActivity.this, "User already exists.", Toast.LENGTH_SHORT).show();
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(SigninActivity.this, "Email or Password are wrong.", Toast.LENGTH_SHORT).show();
                            } else if (exp instanceof FirebaseNetworkException) {
                                Toast.makeText(SigninActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SigninActivity.this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }

        } else {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            finish();
        }
    }

}