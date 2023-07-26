package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.model.Game;
import com.example.myapp.model.GameSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GameListActivity extends AppCompatActivity {

    public static final int ALL_GAMES = 100;
    public static final int GAME_WITH_CODE = 101;
    public static final int GAME_BY_NAME = 102;

    private LinearLayout gameListLayout;
    private DatabaseReference dr;
    private ValueEventListener vel;
    private ArrayList<Game> games;

    private boolean needRemoveListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        overridePendingTransition(0, 0);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setUpdater();

        drawBackButton();
		drawSearchButton();
    }


    private void drawGameTablet(Game game) {
        if (game.getGameSettings().getGameMode() == GameSettings.ONLINE_GAME) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            RelativeLayout childLayout = (RelativeLayout) inflater.inflate(R.layout.game_in_gamelist,
                    (RelativeLayout) findViewById(R.id.game_in_gamelist_layout), false);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.width = getDpAsPixels(155);
            params.height = getDpAsPixels(50);
            params.bottomMargin = getDpAsPixels(5);
            childLayout.setLayoutParams(params);

            TextView gameStatusInfo = childLayout.findViewById(R.id.game_status_info_text);
            String gameStatusStr;
            if (game.getGameStatus() == Game.GAME_NOT_BEGIN) {
                gameStatusStr = "Игра не началась";
                gameStatusInfo.setTextColor(getColor(R.color.greenDark));
            } else if (game.getGameStatus() == Game.IN_GAME) {
                gameStatusStr = "Игра в процессе";
                gameStatusInfo.setTextColor(getColor(R.color.red));
            } else {
                gameStatusStr = "Игра окончена";
            }
            gameStatusInfo.setText(gameStatusStr + " | " + game.getGameCode());

            TextView counts = childLayout.findViewById(R.id.places_in_game);
            System.out.println(game.getGameCode());
            counts.setText(game.getPlayers().size() + "/" + game.getGameSettings().getPlayersCount());

            ImageView joinButton = childLayout.findViewById(R.id.join_to_game_button);
            joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeneralFunctions.joinToGame(GameListActivity.this, game);
                }
            });

            ImageView settingsButton = childLayout.findViewById(R.id.show_settings_button);
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeneralFunctions.showSettingsOfGameLayout(GameListActivity.this, game);
                }
            });

            gameListLayout.addView(childLayout);
        }
    }

    private void drawGameList(int listMode, int gameCode) {
        gameListLayout = findViewById(R.id.game_list_layout);
        gameListLayout.removeAllViews();
        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            boolean needDrawThisGame = false;
            if(listMode == ALL_GAMES || (listMode == GAME_WITH_CODE && game.getGameCode() == gameCode))
                needDrawThisGame = true;

            if(needDrawThisGame)
                drawGameTablet(game);
        }
    }

    private void addNewGame(Game game) {
        boolean isNew = true;
        if (game != null) {
            for (int i = 0; i < games.size(); i++) {
                if (game.getGameCode() == games.get(i).getGameCode()) {
                    isNew = false;
                    games.set(i, game);
                }
            }
            if (isNew) {
                games.add(game);
            }
        }
    }

    private int getDpAsPixels(int valueInDp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (valueInDp * scale + 0.5f);
    }

    private void drawBackButton() {

        ImageView backButton = findViewById(R.id.back_button_in_game_list_activity);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removeListener();
                Intent intent = new Intent(GameListActivity.this, GameModeActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });
    }

    private void drawSearchButton() {
        ImageView searchButton = findViewById(R.id.search_game_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(GameListActivity.this);
                dialog.setContentView(R.layout.enter_code_layout);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(true);
                dialog.show();

                EditText code = dialog.findViewById(R.id.code_input);
                code.setInputType(InputType.TYPE_CLASS_NUMBER);

                TextView enter = dialog.findViewById(R.id.join_to_game);
                enter.setText("Найти");
                enter.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            enter.setBackground(getDrawable(R.color.greenCB));
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            enter.setBackground(getDrawable(R.color.blackCB));
                            String codeStr = code.getText().toString();
                            int codeOfGame;
                            if (!codeStr.equals("")) {
                                try {
                                    codeOfGame = Integer.parseInt(code.getText().toString());
                                    drawGameList(GAME_WITH_CODE, codeOfGame);
                                } catch (NumberFormatException nfe) {
                                    Toast.makeText(getBaseContext(), "Неправильный ввод!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getBaseContext(), "Вы ничего не ввели!", Toast.LENGTH_SHORT).show();
                            }

                            dialog.dismiss();
                        }
                        return true;
                    }
                });

                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void setUpdater() {
        dr = GeneralFunctions.getReferenceToGameList();
        vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                games = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds != null) {
                        int gameCode = ds.getValue(Integer.class);
                        GeneralFunctions.getReferenceToGame(gameCode).child("game")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(needRemoveListeners) {
                                            GeneralFunctions.getReferenceToGame(gameCode).child("game").removeEventListener(this);
                                        }
                                        else {
                                            Game game = snapshot.getValue(Game.class);
                                            if (game != null) {
                                                if (game.getPlayers() != null) {
                                                    addNewGame(game);
                                                    drawGameList(ALL_GAMES, 0);
                                                }
                                            /*else FirebaseDatabase.getInstance(Constants.DATABASE_URL)
                                                    .getReference("game" + gameCode).removeValue();*/
                                            }
                                        /*else FirebaseDatabase.getInstance(Constants.DATABASE_URL)
                                                .getReference("game" + gameCode).removeValue();*/
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        dr.addValueEventListener(vel);
    }

    public void removeListener() {
        dr.removeEventListener(vel);
        needRemoveListeners = true;
    }
}