package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapp.io.Dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        Intent intent = new Intent(this, GameModeActivity.class);
        startActivity(intent);

        InputStream is = openInputStream("word_rus.txt");
        Dictionary.loadDict(is);


        /*FileInputStream fis = openInputStream1("lastGame.txt");
        BufferedReader bw = new BufferedReader(new InputStreamReader(fis));
        try {
            Toast.makeText(getBaseContext(), bw.readLine(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Проблема!", Toast.LENGTH_SHORT).show();
        }*/

        /*try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        finish();
    }

    private InputStream openInputStream(String filename) {
        try {
            return getAssets().open(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private FileOutputStream openOutputStream(String filename) {

        try {
            return openFileOutput(filename, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Toast.makeText(getBaseContext(), "Файл не найден!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return null;
    }

    private FileInputStream openInputStream1(String filename) {
        try {
            return openFileInput(filename);
        } catch (FileNotFoundException e) {
            Toast.makeText(getBaseContext(), "Файл не найден!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return null;
    }
}