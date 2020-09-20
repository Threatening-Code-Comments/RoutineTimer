package com.example.routinetimer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton setupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO hardcode
        Intent intent = new Intent(MainActivity.this, SetRoutine.class);
        startActivity(intent);

        setupButton = findViewById(R.id.btn_mainActivity_setup);
        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetRoutine.class);
                startActivity(intent);
            }
        });
    }
}