package com.example.testapp;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class signup extends AppCompatActivity{
    private EditText mail, mophone, pswd, usrusr;
    TextView lin, sup;
    private FirebaseAuth mAuth;
    private static final String TAG = "signup";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Layout rendering
        sup = (TextView) findViewById(R.id.sup);
        lin = (TextView) findViewById(R.id.lin);
        usrusr = (EditText) findViewById(R.id.usrusr);
        pswd = (EditText) findViewById(R.id.pswrdd);
        mail = (EditText) findViewById(R.id.mail);
        mophone = (EditText) findViewById(R.id.mobphone);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");
        mophone.setTypeface(custom_font);
        sup.setTypeface(custom_font1);
        pswd.setTypeface(custom_font);
        lin.setTypeface(custom_font);
        usrusr.setTypeface(custom_font);
        mail.setTypeface(custom_font);

        sup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent it = new Intent(signup.this, login.class);
//                startActivity(it);
                createAccount(mail.getText().toString(), pswd.getText().toString());
//                Toast.makeText(signup.this, "Acc created",
//                        Toast.LENGTH_SHORT).show();
            }
        });
        lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(signup.this, login.class);
                startActivity(it);
            }
        });


        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

//        showProgressBar();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(signup.this, "Success!e",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(signup.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }


    private boolean validateForm() {
        boolean valid = true;

//        String username = usrusr.getText().toString();
//        if (TextUtils.isEmpty(username)) {
//            usrusr.setError("Required.");
//            valid = false;
//        } else {
//            usrusr.setError(null);
//        }

        String email = mail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mail.setError("Required.");
            valid = false;
        } else {
            mail.setError(null);
        }

        String password = pswd.getText().toString();
        if (TextUtils.isEmpty(password)) {
            pswd.setError("Required.");
            valid = false;
        } else {
            pswd.setError(null);
        }

//        String mobile_no = mophone.getText().toString();
//        if (TextUtils.isEmpty(mobile_no)) {
//            mophone.setError("Required.");
//            valid = false;
//        } else {
//            mophone.setError(null);
//        }


        return valid;
    }

//
//    public void onClick (View v) {
//        int i = v.getId();
//        if (i == R.id.sup) {
//            createAccount(mail.getText().toString(), pswd.getText().toString());
//        }
//    }


}
