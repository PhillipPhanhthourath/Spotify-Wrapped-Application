package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WrappedEndScreenActivity extends AppCompatActivity {
    private View frontCard;
    private View backCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_of_wrapped);

        // view on arrival to page
        TextView title = findViewById(R.id.title_text);
        Button buttonBack = findViewById(R.id.back_button);
        Button buttonContinue = findViewById(R.id.continue_button);
        frontCard = findViewById(R.id.summary_image);
        TextView gamePrompt = findViewById(R.id.game_prompt);

        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));
        // backCard.startAnimation(WrappedHelper.animation(this, "fade in"));

        gamePrompt.setVisibility(View.VISIBLE);
        Animation flyInBottom = WrappedHelper.animation(this, "fly in bottom");
        gamePrompt.startAnimation(flyInBottom);
        Animation floatVertical = WrappedHelper.animation(this, "float");
        // gamePrompt.startAnimation(floatVertical);
        Animation fadeInWithOffset = WrappedHelper.animation(this, "fade in");
        fadeInWithOffset.setStartOffset(3000);
        buttonContinue.startAnimation(fadeInWithOffset);
        buttonBack.startAnimation(fadeInWithOffset);

        // Set the click listeners for the buttons

        buttonBack.setOnClickListener((v) -> {
            returnToMain();
        });

        buttonContinue.setOnClickListener((v) -> {
            continueToGame();
        });

        /*frontCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });

        /* backCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });*/
    }

    /**
     * Return to the main page
     */
    public void returnToMain() {
        Intent intent = new Intent(WrappedEndScreenActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    public void continueToGame() {
        Intent intent = new Intent(WrappedEndScreenActivity.this, GameNavActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }
}
