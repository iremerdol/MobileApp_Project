package com.example.teamproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity{
    private EditText editTextMail, editTextPass,editTextPass2;
    private Button buttonSignUp,buttonLoginPage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(register.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        editTextMail = findViewById(R.id.editTextMail);
        editTextPass = findViewById(R.id.editTextPass);
        editTextPass2 = findViewById(R.id.editTextPass2);

        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonLoginPage = findViewById(R.id.buttonLoginPage);

        // Set a click listener for the login button
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = editTextMail.getText().toString();
                String password = editTextPass.getText().toString();
                String password2 = editTextPass2.getText().toString();

                if(mail.isEmpty() || password.isEmpty() || password2.isEmpty()){
                    Toast.makeText(register.this, "Please fulfill the mail and password.",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    if (password.equals(password2)) {
                        mAuth.createUserWithEmailAndPassword(mail, password)
                                .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("email", mail);
                                            user.put("pass", password);
                                            user.put("balance", 0);
                                            user.put("gifts" , 0);

                                            // Add a new document with a generated ID
                                            db.collection("users").document(mAuth.getUid().toString()).set(user);

                                            Intent intent = new Intent(register.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(register.this, task.getException().toString(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        Toast.makeText(register.this, "Register successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(register.this, "The passwords does not match.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(register.this, login.class);
                startActivity(intent);
                finish(); // Close the login activity
            }
        });
    }

}
