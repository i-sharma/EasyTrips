package com.example.testapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnTouchListener{
    TextView google_signin,phone_signin, skip, welcome;
    private static final String TAG = "login";
    public static Activity activity;
    ProgressBar progressBar;

    private static final int RC_SIGN_IN = 9001;
    private FirebaseFirestore rootRef;
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;

    private SignInButton gSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activity = this;
        progressBar = findViewById(R.id.login_progress);
        //Google Signin

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();


        //login functionality
        google_signin = (TextView) findViewById(R.id.google_login);
        phone_signin = (TextView) findViewById(R.id.phone_login);
        skip = findViewById(R.id.login_skip);
        welcome = findViewById(R.id.welcome_msg);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");
        google_signin.setTypeface(custom_font1);
        phone_signin.setTypeface(custom_font1);
        skip.setTypeface(custom_font);
        welcome.setTypeface(custom_font);
        skip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(LoginActivity.this, ExploreActivity.class);
                startActivity(it);
                finish();
            }
        });
        google_signin.setOnTouchListener(this);
        phone_signin.setOnTouchListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            setNameInFirestore(user.getUid(), user.getDisplayName());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void setNameInFirestore(final String uid, final String displayName) {
        rootRef.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "here we are sir2");
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.get("name").toString();
                                if (name == null || name == "") {
                                    setName(uid, displayName);
                                }
                            } else {
                                setName(uid, displayName);
                            }

                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

    }

    private void setName(String uid, String displayName) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", displayName);
        rootRef.collection("users")
                .document(uid)
                .set(data, SetOptions.merge());
    }

    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);
        if (user != null){
            Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, ExploreActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(LoginActivity.this, "Not Signed In", Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            v.setBackgroundColor(getResources().getColor(R.color.auth_button_light));
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            v.setBackgroundColor(getResources().getColor(R.color.auth_button));
            switch (v.getId()){
                case R.id.google_login:
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user == null)
                        signIn();
                    break;
                case R.id.phone_login:
                    Intent it = new Intent(LoginActivity.this, PhoneAuthActivity.class);
                    startActivity(it);
                    break;
            }
        }
        return true;
    }
}
