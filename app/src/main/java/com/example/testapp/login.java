package com.example.testapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity {
    TextView google_signin,phone_signin, skip, welcome;
    private static final String TAG = "login";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


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
        phone_signin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(login.this, phoneAuth.class);
                startActivity(it);
            }
        });
        skip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(login.this, Explore.class);
                startActivity(it);
            }
        });
        google_signin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(login.this, googleSignIn.class);
                startActivity(it);
            }
        });
    }
}
