package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WrappedEndScreenActivity extends AppCompatActivity {
    private TextView title;
    private View frontCard;
    private View backCard;
    private TextView gamePrompt;
    private Button buttonContinue;
    private Button buttonBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_of_wrapped);

        // view on arrival to page
        title = findViewById(R.id.title_text);
        buttonBack = findViewById(R.id.back_button);
        buttonContinue = findViewById(R.id.continue_button);
        frontCard = findViewById(R.id.summary_image);
        backCard = findViewById(R.id.text_card);
        gamePrompt = findViewById(R.id.game_prompt);

        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));
        backCard.startAnimation(WrappedHelper.animation(this, "fade in"));

        gamePrompt.setVisibility(View.VISIBLE);
        gamePrompt.startAnimation(WrappedHelper.animation(this, "fly in bottom"));
        Animation floatWithOffset = WrappedHelper.animation(this, "float");
        floatWithOffset.setStartOffset(1000);
        gamePrompt.startAnimation(floatWithOffset);
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

        frontCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });

        backCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });
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
