package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.model.Game;
import com.example.myapp.model.GameSettings;
import com.example.myapp.model.Player;
import com.example.myapp.model.Roles;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GameSettingsActivity extends AppCompatActivity {

    private GameSettings gameSettings = new GameSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_game_settings);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bundle bundle = getIntent().getExtras();
        int gameMode = bundle.getInt(GameModeActivity.GAME_MODE_ATTRIBUTE_NAME);
        gameSettings.setGameMode(gameMode);

        if(gameMode == GameSettings.ONLINE_GAME) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.type_settings_layout);
            linearLayout.setVisibility(View.GONE);
        }


        ///////////////////////
        TextView twoDevices = (TextView) findViewById(R.id.two_devices_text);
        twoDevices.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setDevicesType(GameSettings.TWO_DEVICES);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.type_settings_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                twoDevices.setTextColor(getColor(R.color.green));
                findViewById(R.id.vote_type_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.team_selection_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.players_count_layout).setVisibility(View.INVISIBLE);
                return true;
            }
        });

        TextView manyDevices = (TextView) findViewById(R.id.many_devices_text);
        manyDevices.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setDevicesType(GameSettings.MANY_DEVICES);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.type_settings_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                manyDevices.setTextColor(getColor(R.color.green));
                findViewById(R.id.vote_type_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.team_selection_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.players_count_layout).setVisibility(View.VISIBLE);
                return true;
            }
        });



        /////////////////////////
        TextView playersCount = findViewById(R.id.players_count);
        playersCount.setText(gameSettings.getPlayersCount() + "");

        ImageView up = findViewById(R.id.up_arrow);
        ImageView down = findViewById(R.id.down_arrow);

        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (gameSettings.getPlayersCount() < GameSettings.MAX_PLAYERS_COUNT) {
                        gameSettings.incrementPlayersCount();
                        TextView playersCount = (TextView) findViewById(R.id.players_count);
                        playersCount.setText(gameSettings.getPlayersCount() + "");
                    }
                }
                return true;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (gameSettings.getPlayersCount() > GameSettings.MIN_PLAYERS_COUNT) {
                        gameSettings.decrementPlayersCount();
                        TextView playersCount = (TextView) findViewById(R.id.players_count);
                        playersCount.setText(gameSettings.getPlayersCount() + "");
                    }
                }
                return true;
            }
        });



        ///////////////////////////
        TextView anyoneCanClick = (TextView) findViewById(R.id.anyone_can_click_text);
        anyoneCanClick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setWordSelectionType(GameSettings.ANYONE_CAN_CLICK);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.vote_type_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                anyoneCanClick.setTextColor(getColor(R.color.green));
                return true;
            }
        });

        TextView allVote = (TextView) findViewById(R.id.team_vote_text);
        allVote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setWordSelectionType(GameSettings.TEAM_VOTE);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.vote_type_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                allVote.setTextColor(getColor(R.color.green));
                return true;
            }
        });

        TextView onePlayerCanClick = (TextView) findViewById(R.id.one_player_click_text);
        onePlayerCanClick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setWordSelectionType(GameSettings.ONE_PLAYER_CAN_CLICK);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.vote_type_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                onePlayerCanClick.setTextColor(getColor(R.color.green));
                return true;
            }
        });

        TextView randomPlayerClick = (TextView) findViewById(R.id.random_player_click_text);
        randomPlayerClick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setWordSelectionType(GameSettings.RANDOM_PLAYER_CAN_CLICK);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.vote_type_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                randomPlayerClick.setTextColor(getColor(R.color.green));
                return true;
            }
        });



        ///////////////////////////
        TextView freeTeamSelection = (TextView) findViewById(R.id.free_team_choose_text);
        freeTeamSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setTeamSelectionType(GameSettings.FREE_TEAM_CHOOSE);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.team_selection_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                freeTeamSelection.setTextColor(getColor(R.color.green));
                return true;
            }
        });

        TextView randomTeamSelection = (TextView) findViewById(R.id.random_team_selection_text);
        randomTeamSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setTeamSelectionType(GameSettings.RANDOM_TEAM_SELECTION);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.team_selection_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                randomTeamSelection.setTextColor(getColor(R.color.green));
                return true;
            }
        });

        TextView creatorTeamSelection = (TextView) findViewById(R.id.creator_distributes_team_text);
        creatorTeamSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameSettings.setTeamSelectionType(GameSettings.TEAM_SELECTION_BY_CREATOR);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.team_selection_layout);
                ArrayList<TextView> texts = new ArrayList<>();
                getAllTextViews(linearLayout, texts);
                for (TextView text:texts) {
                    text.setTextColor(getColor(R.color.white));
                }
                creatorTeamSelection.setTextColor(getColor(R.color.green));
                return true;
            }
        });


        TextView createGame = findViewById(R.id.create_game);
        createGame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    createGame.setBackground(getResources().getDrawable(R.drawable.blue_tablet2));
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    createGame.setBackground(getResources().getDrawable(R.drawable.blue_tablet1));
                    boolean flag = true;
                    if(gameSettings.getGameMode() == GameSettings.ONLINE_GAME) {
                        if(gameSettings.getWordSelectionType() == GameSettings.PARAMETER_NOT_DEFINED ||
                                gameSettings.getTeamSelectionType() == GameSettings.PARAMETER_NOT_DEFINED)
                            flag = false;
                    }
                    else if(gameSettings.getGameMode() == GameSettings.GAME_WITH_FRIENDS) {
                        if(gameSettings.getDevicesType() == GameSettings.TWO_DEVICES) gameSettings.setPlayersCount(2);

                        if(gameSettings.getDevicesType() == GameSettings.PARAMETER_NOT_DEFINED) {
                            flag = false;
                        }
                        else if(gameSettings.getDevicesType() == GameSettings.MANY_DEVICES) {
                            if(gameSettings.getWordSelectionType() == GameSettings.PARAMETER_NOT_DEFINED ||
                                    gameSettings.getTeamSelectionType() == GameSettings.PARAMETER_NOT_DEFINED)
                                flag = false;
                        }
                    }
                    if(flag) {
                        Game game = new Game(gameSettings);
                        game.startTheGame();
                        try {
                            FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference("game" + game.getGameCode()).child("game").setValue(game);
                            FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference("game_list")
                                    .child("game" + game.getGameCode()).setValue(game.getGameCode());
                        }
                        catch(Exception ex) {
                            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        Toast.makeText(getBaseContext(), "Игра создана!", Toast.LENGTH_SHORT).show();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        assert user != null;
                        Player player = new Player(user.getDisplayName(), user.getUid(), user.getEmail());

                        if(gameSettings.getDevicesType() == GameSettings.TWO_DEVICES) {
                            player.setRole(Roles.CAPTAIN);
                            player.setTeamOfPlayer(Player.BOTH_TEAMS_PLAYER);
                        }
                        game.acceptToGame(player);
                        Intent intent = new Intent(GameSettingsActivity.this, LobbyActivity.class);
                        intent.putExtra("game", game);
                        intent.putExtra("player", player);

                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(getBaseContext(), "Не все настройки игры выбраны!", Toast.LENGTH_SHORT).show();
                    }
                    createGame.setTextColor(getColor(R.color.black));

                }
                return true;
            }
        });

        drawBackButton();
    }

    private void drawBackButton() {
        ImageView backButton = findViewById(R.id.back_button_in_settings_activity);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(GameSettingsActivity.this, GameModeActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });
    }

    public void getAllTextViews(ViewGroup layout, ArrayList<TextView> texts){
        for(int i = 0; i < layout.getChildCount(); i++){
            View view = layout.getChildAt(i);
            if(view instanceof TextView){
                texts.add((TextView)view);
            }
            else if(view instanceof ViewGroup) {
                getAllTextViews((ViewGroup) view, texts);
            }
        }
    }
}