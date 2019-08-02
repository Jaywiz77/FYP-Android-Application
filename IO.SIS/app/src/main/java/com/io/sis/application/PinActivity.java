package com.io.sis.application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class PinActivity extends AppCompatActivity {

    EditText participantPin;
    Button enterButton;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        participantPin=findViewById(R.id.participantPin);
        enterButton=findViewById(R.id.login);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(Objects.equals(username.getText().toString(), "123")&&Objects.equals(password.getText().toString(),"123"))

                if(Objects.equals(participantPin.getText().toString(),"1234"))
                {
                    //Toast.makeText(PinActivity.this,"You have Authenticated Successfully",Toast.LENGTH_LONG).show();
                    Intent i = new Intent(PinActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();

                    pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
                    editor = pref.edit();
                    editor.putString("participantPin", participantPin.getText().toString());
                    editor.apply();

                }else
                {
                    // wrong
                    Toast.makeText(PinActivity.this,"Invalid Pin",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
