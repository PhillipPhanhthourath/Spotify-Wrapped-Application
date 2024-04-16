package com.example.spotifywrappedapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WrappedEndScreenActivity extends AppCompatActivity {
    private View frontCard;
    private View backCard;
    private View downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_of_wrapped);

        // view on arrival to page
        TextView title = findViewById(R.id.title_text);
        Button buttonBack = findViewById(R.id.back_button);
        Button buttonContinue = findViewById(R.id.continue_button);
        frontCard = findViewById(R.id.summary_image);
        backCard = findViewById(R.id.download_background);
        downloadButton = findViewById(R.id.download_button);
        TextView gamePrompt = findViewById(R.id.game_prompt);

        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));

        gamePrompt.setVisibility(View.VISIBLE);
        Animation flyInBottom = WrappedHelper.animation(this, "fly in bottom");
        gamePrompt.startAnimation(flyInBottom);
        Animation floatVertical = WrappedHelper.animation(this, "float");
        gamePrompt.startAnimation(floatVertical);
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
            downloadButton.setVisibility(View.VISIBLE);
            downloadButton.startAnimation(WrappedHelper.animation(this, "fade in"));
        });

        backCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
            downloadButton.startAnimation(WrappedHelper.animation(this, "fade out"));
            downloadButton.setVisibility(View.INVISIBLE);
        });

        downloadButton.setOnClickListener((v) -> {
            downloadImage(backCard);
        });
    }

    /**
     * Download the final wrapped summary onto the device
     */
    public void downloadImage(View image) {
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.draw(canvas);
        String filename = "summary_image.jpeg";
        FileOutputStream outputStream;
        try {
            outputStream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Toast.makeText(this, "Successfully downloaded!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Return to the main page
     */
    public void returnToMain() {
        Intent intent = new Intent(WrappedEndScreenActivity.this, MainActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
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
