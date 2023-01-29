package com.example.nabeatsurally;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);



        Button sendButton = findViewById(R.id.return_button);
        sendButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplication(), PlayModeActivity.class);
            startActivity(intent);
        });

    }

}