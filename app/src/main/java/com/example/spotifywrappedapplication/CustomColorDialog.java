package com.example.spotifywrappedapplication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class CustomColorDialog extends Dialog {

    private String color;
    private TextView textView;
    private Button closeButton;

    public CustomColorDialog(Context context, String color) {
        super(context);
        this.color = color;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_layout);

        textView = findViewById(R.id.dialog_text);
        closeButton = findViewById(R.id.close_button);

        // Set the color text
        textView.setText("Color: " + color);

        // Set click listener to close the dialog
        closeButton.setOnClickListener(v -> dismiss());

        // Customize the dialog size
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Full width
            layoutParams.height = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.875); // 7/8 height
            window.setAttributes(layoutParams);
        }
    }
}
