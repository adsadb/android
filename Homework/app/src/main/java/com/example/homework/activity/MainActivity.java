package com.example.homework.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homework.R;

public class MainActivity extends AppCompatActivity {
    private ImageView photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void turnToPostImage(View view){
        Intent intent = new Intent(MainActivity.this,PostImageActivity.class);
        startActivity(intent);
    }

    public void turnToShowImage(View view){
        Intent intent = new Intent(MainActivity.this,ShowResultActivity.class);
        startActivity(intent);
    }
}
