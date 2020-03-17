package com.example.testapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class login extends AppCompatActivity {
    EditText pswd,usrusr;
    TextView sup,lin;
    private static final String TAG = "login";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        JSONObject obj;
//        //firebase
//        Map<String, Object> user = new HashMap<>();
//        user.put("first", "Ishan");
//        user.put("last", "Sharma");
//        user.put("born", 2000);
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });


        //upload to firestore
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("firebase_data.json")));

            // do reading, usually loop until end of file reading
            String mLine ;
            while ((mLine = reader.readLine()) != null) {
                //process line
                obj = (JSONObject) new JSONObject(mLine);
                JSONObject obj1;
                int i;
//                JSONObject obj1 = obj.getJSONObject("delhi::1");
//                Log.d(TAG, "here hehe " + obj.getJSONObject("delhi::1").get("title") );
                for (i = 1; i < 66; i++){
                    String name = "delhi::" + Integer.toString(i);
                    obj1 = obj.getJSONObject(name);

                    Iterator<String> keys = obj1.keys();
                    Map<String, Object> user = new HashMap<>();
                    while( keys.hasNext() ) {
                        String key = (String) keys.next();
//                    Log.d(TAG, "here hehe key: " + key + " value: " + obj1.get(key) );
                        user.put(key, obj1.get(key));
                    }
                    user.put("city", "delhi");

                    // Add a new document with a generated ID
                    db.collection("ts_data")
                            .document("delhi")
                            .collection("delhi_data")
                            .document(name)
                            .set(user);
                    Log.d(TAG, "done " + Integer.toString(i));

                }


            }
        } catch (IOException e) {
            //log the exception
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        //login functionality
        lin = (TextView) findViewById(R.id.lin);
        usrusr = (EditText) findViewById(R.id.usrusr);
        pswd = (EditText) findViewById(R.id.pswrdd);
        sup = (TextView) findViewById(R.id.sup);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");
        lin.setTypeface(custom_font1);
        sup.setTypeface(custom_font);
        usrusr.setTypeface(custom_font);
        pswd.setTypeface(custom_font);
        sup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(login.this, signup.class);
                startActivity(it);
            }
        });
        lin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(login.this, Explore.class);
                startActivity(it);
            }
        });
    }
}
