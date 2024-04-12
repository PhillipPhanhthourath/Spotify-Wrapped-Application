package com.example.spotifywrappedapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GameNavActivity extends AppCompatActivity implements PlaceholderAdapter.OnItemClicked {

    private RecyclerView recyclerView;
    private PlaceholderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_nav);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaceholderAdapter(new ArrayList<FragmentData>(), this);
        recyclerView.setAdapter(adapter);
        adapter.updateData(new FragmentData("#FF0000"));
        adapter.updateData(new FragmentData("#00FF00"));
        adapter.updateData(new FragmentData("#0000FF"));
        Button openAlertButton = findViewById(R.id.openAlertButton);
        openAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GameNavActivity.this)
                        .setMessage("This is an empty alert.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onItemClick(FragmentData fragmentData) {
        CustomColorDialog dialog = new CustomColorDialog(this, fragmentData.getColor());
        dialog.show();
    }
}
