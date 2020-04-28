package com.example.socialnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassowrdActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button ResetPassowrdSendEmailButton;
    private EditText ResetEmailInput;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_passowrd);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.forget_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Passowrd");

        ResetPassowrdSendEmailButton = (Button) findViewById(R.id.reset_password_email_button);
        ResetEmailInput = (EditText) findViewById(R.id.reset_passowrd_email);

        ResetPassowrdSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = ResetEmailInput.getText().toString();
                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPassowrdActivity.this, "Please write your vaild email", Toast.LENGTH_SHORT).show();
                } else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPassowrdActivity.this, "Please check your email account, if you want to reset your passord", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPassowrdActivity.this,LoginActivity.class));
                            } else {
                                String massege = task.getException().getMessage();
                                Toast.makeText(ResetPassowrdActivity.this, "Error occured: " + massege, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

    }
}
