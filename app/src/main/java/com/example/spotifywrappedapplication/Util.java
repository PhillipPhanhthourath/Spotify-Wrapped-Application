package com.example.spotifywrappedapplication;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;
public class Util {

    public static void DebugAlert(String message, AppCompatActivity inst
    ){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(inst);
        builder.setTitle("DebugAlert")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the OK button action here
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

}


