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

public class WelcomeScreenActivity extends AppCompatActivity {
    private TextView textView;
    Button buttonContinue;
    Button buttonBack;

    private Animation animation(String anim) {
        anim = anim.toLowerCase().trim();
        if (anim.equals("fly in")) {
            return AnimationUtils.loadAnimation(this, R.anim.fly_in_animation);
        } else if (anim.equals("fly out")) {
            return AnimationUtils.loadAnimation(this, R.anim.fly_out_animation);
        } else if (anim.equals("fade in")) {
            return AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        } else if (anim.equals("fade out")) {
            return AnimationUtils.loadAnimation(this, R.anim.fade_out_animation);
        }

        throw new IllegalArgumentException("animation not available.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_to_wrapped);

        // view on arrival to page
        textView = findViewById(R.id.flying_text);
        buttonBack = findViewById(R.id.back_button);
        buttonContinue = findViewById(R.id.continue_button);
        textView.setVisibility(View.VISIBLE);
        textView.startAnimation(animation("fly in"));
        buttonContinue.startAnimation(animation("fade in"));
        buttonBack.startAnimation(animation("fade in"));


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
        Intent intent = new Intent(WelcomeScreenActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    public void continueToWrapped() {
        Intent intent = new Intent(WelcomeScreenActivity.this, WrappedPartOneActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }


    private class CallbackHandler implements Callback {
        /*
        In this example, we simply populate the textview object with the text returned from the request
        Need to define different CallbackHandler's depending on what you want to do with the text returned from the request!
         */
        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> textView.setText("Error: " + e.getMessage()));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            runOnUiThread(() -> textView.setText(result));
        }
    }
}