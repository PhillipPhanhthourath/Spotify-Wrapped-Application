package com.example.spotifywrappedapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class DialogUtils {
    public static void showSignInDialog(Context context, AuthDialogListener listener) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_corners)); // Set rounded corners background
        layout.setPadding(40, 40, 40, 40); // Add padding inside the layout

        EditText emailInput = new EditText(context);
        emailInput.setHint("Email");
        emailInput.setTextColor(Color.parseColor("#6b4dac")); // Purple text color
        emailInput.setHintTextColor(Color.parseColor("#6b4dac")); // Lighter purple hint text
        emailInput.setTypeface(Typeface.SANS_SERIF); // Set sans-serif font
        layout.addView(emailInput);

        EditText passwordInput = new EditText(context);
        passwordInput.setHint("Password");
        passwordInput.setTextColor(Color.parseColor("#6b4dac")); // Purple text color
        passwordInput.setHintTextColor(Color.parseColor("#6b4dac")); // Lighter purple hint text
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // Mask password
        passwordInput.setTypeface(Typeface.SANS_SERIF); // Set sans-serif font
        layout.addView(passwordInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme); // Custom style for dialog
        builder.setTitle("Sign In / Register");
        builder.setView(layout);
        builder.setPositiveButton("Sign In", (dialog, which) -> {
            listener.onSignIn(emailInput.getText().toString(), passwordInput.getText().toString(), context);
        });
        builder.setNegativeButton("Register", (dialog, which) -> {
            listener.onRegister(emailInput.getText().toString(), passwordInput.getText().toString(), context);
        });
        AlertDialog dialog = builder.show(); // Display the dialog first
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        positiveButton.setTextColor(Color.BLUE);
        negativeButton.setTextColor(Color.RED);

    }



    public interface AuthDialogListener {
        void onSignIn(String email, String password, Context context );
        void onRegister(String email, String password, Context context);
    }
}
