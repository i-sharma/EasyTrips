package com.example.testapp;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class signup extends AppCompatActivity implements View.OnClickListener{
    private EditText mail, mophone, pswd, usrusr;
    TextView lin, sup;
    private FirebaseAuth mAuth;
    private static final String TAG = "signup";
    private boolean mVerificationInProgress = false;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private static final int STATE_CODE_SENT = 1;
    private static final int STATE_SIGNIN_SUCCESS = 2;
    private static final int STATE_SIGNIN_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;

    private FirebaseFirestore rootRef;


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

        rootRef = FirebaseFirestore.getInstance();

        sup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        lin.setOnClickListener(this);
        sup.setOnClickListener(this);


        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;
                updateUI(STATE_VERIFY_SUCCESS);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    mophone.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                updateUI(STATE_CODE_SENT);


                // [END_EXCLUDE]
            }
        };
    }
//
//    private void updateUI(int state){
//        updateUI(state, null);
//    }


    private void updateUI(int state){
        FirebaseUser user;
//        Toast.makeText(signup.this, message, Toast.LENGTH_SHORT).show();
        switch (state){
            case STATE_CODE_SENT:
                LinearLayout otp = findViewById(R.id.otp);
                otp.setVisibility(View.VISIBLE);
                sup.setText("SignIn");
                break;
            case STATE_SIGNIN_SUCCESS:
//
                user = mAuth.getCurrentUser();
                if(user!=null)
                    Toast.makeText(signup.this, user.getDisplayName(),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(signup.this, "Something's not quite right",
                            Toast.LENGTH_SHORT).show();
                break;
            case STATE_SIGNIN_FAILED:
                Toast.makeText(signup.this, "SignIn Failed", Toast.LENGTH_SHORT).show();
                break;
            case STATE_VERIFY_SUCCESS:
//                pswd.setText();

        }
    }


    private boolean validatePhoneNumber() {
        String phoneNumber = mophone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mophone.setError("Can't be empty");
            return false;
        }
        if(phoneNumber.length() != 10){
            mophone.setError("Enter 10 digits");
            return false;
        }
        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        startPhoneNumberVerification(phoneNumber, "+91");
    }

    private void startPhoneNumberVerification(String phoneNumber, String country_code) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                country_code + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lin:
                Intent it = new Intent(signup.this, login.class);
                startActivity(it);
                break;
            case R.id.sup:
                if(sup.getText() == "SignIn"){
                    String code = pswd.getText().toString();
                    if (TextUtils.isEmpty(code)) {
                        pswd.setError("Cannot be empty.");
                        return;
                    }

                    verifyPhoneNumberWithCode(mVerificationId, code);
                    break;
                }

                if(validatePhoneNumber()){
                    startPhoneNumberVerification(mophone.getText().toString());
                }
                else{
                    Toast.makeText(signup.this, "Tumse na ho paaega",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
//                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();

                            if(user!=null) {
                                Log.d(TAG, "here we are sir");
                                if(rootRef == null)
                                    return;
                                Log.d(TAG, "here we are sir1");
                                rootRef.collection("users")
                                        .document(user.getUid())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                Intent i;
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "here we are sir2");
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                        if(document.get("name").toString() == null || document.get("name").toString() == "") {
                                                            i = new Intent(signup.this, userName.class);
                                                            startActivity(i);
                                                        }
                                                        else{
                                                            i = new Intent(signup.this, Explore.class);
                                                            startActivity(i);
                                                        }
                                                    } else {
                                                        Log.d(TAG, "No such document");
                                                        i = new Intent(signup.this, userName.class);
                                                        startActivity(i);
                                                    }
                                                } else {
                                                    Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });
//                                updateUI(STATE_SIGNIN_SUCCESS);
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                pswd.setError("Invalid code.");
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });

    }
}
