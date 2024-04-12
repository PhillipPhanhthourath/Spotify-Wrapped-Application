package com.example.spotifywrappedapplication;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

public class DialogUtils {
    public static void showSignInDialog(Context context, AuthDialogListener listener) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText emailInput = new EditText(context);
        emailInput.setHint("Email");
        layout.addView(emailInput);
        EditText passwordInput = new EditText(context);
        passwordInput.setHint("Password");
        layout.addView(passwordInput);

        new AlertDialog.Builder(context)
                .setTitle("Sign In / Register")
                .setView(layout)
                .setPositiveButton("Sign In", (dialog, which) -> {
                    listener.onSignIn(emailInput.getText().toString(), passwordInput.getText().toString());
                })
                .setNegativeButton("Register", (dialog, which) -> {
                    listener.onRegister(emailInput.getText().toString(), passwordInput.getText().toString());
                })
                .show();
    }

    public interface AuthDialogListener {
        void onSignIn(String email, String password);
        void onRegister(String email, String password);
    }
}
