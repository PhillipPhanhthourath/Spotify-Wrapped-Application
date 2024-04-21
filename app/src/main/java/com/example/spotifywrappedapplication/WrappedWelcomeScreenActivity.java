package com.example.spotifywrappedapplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WrappedWelcomeScreenActivity extends AppCompatActivity {
    private TextView textView;
    Button buttonContinue;
    Button buttonBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_to_wrapped);

        // view on arrival to page
        textView = findViewById(R.id.flying_text);
        buttonBack = findViewById(R.id.back_button);
        buttonContinue = findViewById(R.id.continue_button);
        textView.setVisibility(View.VISIBLE);
        textView.startAnimation(WrappedHelper.animation(this, "fly in"));
        buttonContinue.startAnimation(WrappedHelper.animation(this, "fade in"));
        buttonBack.startAnimation(WrappedHelper.animation(this, "fade in"));


        // Set the click listeners for the buttons

        buttonBack.setOnClickListener((v) -> {
            returnToMain();
        });

        buttonContinue.setOnClickListener((v) -> {
            continueToWrapped();
        });
    }

    /**
     * Return to the main page
     */
    public void returnToMain() {
        Intent intent = new Intent(WrappedWelcomeScreenActivity.this, MainActivity.class);
        intent.putExtra("ACCESS_TOKEN", this.getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    public void continueToWrapped() {
        Intent intent = new Intent(WrappedWelcomeScreenActivity.this, WrappedPartOneActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }
}