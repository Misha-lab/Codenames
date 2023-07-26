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
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.model.Game;
import com.example.myapp.model.GameSettings;
import com.example.myapp.model.Player;
import com.example.myapp.model.Roles;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class LobbyActivity extends AppCompatActivity {

    private Game game;
    private Player player;
    private int codeOfGame;
    private String playerID = FirebaseAuth.getInstance().getUid();
    private DatabaseReference dr;
    private ValueEventListener vel;
    private boolean isTimerStarted;
    private long timeBeforeBegin = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_lobby);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        Bundle bundle = getIntent().getExtras();
        game = (Game) bundle.get("game");
        player = game.getPlayerById(playerID);
        codeOfGame = game.getGameCode();
        dr = GeneralFunctions.getReferenceToGame(codeOfGame);

        //Toast.makeText(getBaseContext(), "CREATED", Toast.LENGTH_SHORT).show();
        sendGameState();
        setUpdater();
        drawLobbyView();
    }

    private void setUpdater() {
        if (vel == null) {
            vel = new ValueEventListener() {
                public ValueEventListener val = this;

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    game = snapshot.getValue(Game.class);

                    if (game != null) {
                        player = game.getPlayerById(playerID);

                        if (game.getGameStatus() == Game.IN_GAME) {
                            if (!isTimerStarted) {
                                TextView timerMessage = findViewById(R.id.timer);
                                timerMessage.setText(GeneralFunctions.millisToSeconds(timeBeforeBegin) + "");
                                timerMessage.setVisibility(View.VISIBLE);

                                CountDownTimer countDownTimer = new CountDownTimer(timeBeforeBegin, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        timerMessage.setText(GeneralFunctions.millisToSeconds(millisUntilFinished) + "");
                                    }

                                    @Override
                                    public void onFinish() {
                                        timerMessage.setVisibility(View.INVISIBLE);
                                        if(player.getTeamOfPlayer() == Player.TEAM_NOT_SELECTED) {
                                            player.setRandomTeamOfPlayer();
                                            sendGameState();
                                        }
                                        dr.removeEventListener(val);
                                        Intent intent = new Intent(LobbyActivity.this, GameActivity.class);
                                        intent.putExtra("game", game);
                                        startActivity(intent);
                                        finish();
                                    }
                                };
                                countDownTimer.start();
                                isTimerStarted = true;
                            }
                        }
                        if (player != null)
                            drawLobbyView();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            dr.child(GeneralFunctions.GAME_REFERENCE_PATH).addValueEventListener(vel);
        }
    }

    public void removeListener() {
        dr.removeEventListener(vel);
    }

    public void sendGameState() {
        if (game != null)
            dr.child(GeneralFunctions.GAME_REFERENCE_PATH).setValue(game);
        else dr.removeValue();
    }

    private void drawLobbyView() {
        TextView code = findViewById(R.id.code_to_join);
        code.setText(getString(R.string.code_to_join_message) + " " + codeOfGame);

        int[] playersId = {R.id.player0, R.id.player1, R.id.player2, R.id.player3, R.id.player4,
                R.id.player5, R.id.player6, R.id.player7, R.id.player8, R.id.player9};

        for (int i = 0; i < GameSettings.MAX_PLAYERS_COUNT; i++) {
            RelativeLayout playerIcon = findViewById(playersId[i]);
            playerIcon.setVisibility(View.INVISIBLE);
        }

        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player curPlayer = game.getPlayers().get(i);
            RelativeLayout playerIcon = findViewById(playersId[i]);
            //ImageView focus = playerIcon.findViewById(R.id.focus);

            boolean isCanChangeTeam = player.canSelectTeam(game, curPlayer.equals(player));
            /*if(game.getGameSettings().getTeamSelectionType() == GameSettings.TEAM_SELECTION_BY_CREATOR && player.isCreator(game)) {
                playerIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println(isCanChangeTeam);
                        for (int i = 0; i < game.getPlayers().size(); i++) {
                            RelativeLayout otherPlayerIcon = findViewById(playersId[i]);
                            ImageView otherFocus = otherPlayerIcon.findViewById(R.id.focus);
                            otherFocus.setVisibility(View.INVISIBLE);

                            ImageView thisFocus = playerIcon.findViewById(R.id.focus);
                            thisFocus.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }*/
            GeneralFunctions.drawPlayerTablet(LobbyActivity.this, playerIcon, curPlayer, isCanChangeTeam);
        }

        LinearLayout[] rolesButtons = new LinearLayout[3];
        rolesButtons[0] = findViewById(R.id.captain_button_in_lobby);
        rolesButtons[1] = findViewById(R.id.leader_button_in_lobby);
        rolesButtons[2] = findViewById(R.id.usual_player_button_in_lobby);

        if (game.getGameSettings().getWordSelectionType() == GameSettings.ONE_PLAYER_CAN_CLICK)
            rolesButtons[1].setVisibility(View.VISIBLE);
        else rolesButtons[1].setVisibility(View.GONE);


        if (player.getRole() == Roles.CAPTAIN)
            rolesButtons[0].setBackground(getResources().getDrawable(R.drawable.red_tablet1));
        rolesButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getRole() != Roles.CAPTAIN) {
                    for (int i = 0; i < 3; i++) {
                        rolesButtons[i].setBackground(getResources().getDrawable(R.drawable.blue_tablet1));
                    }
                }
                //rolesButtons[0].setBackground(getResources().getDrawable(R.drawable.red_tablet1));
                player.setRole(Roles.CAPTAIN);
                sendGameState();

            }
        });

        if (player.getRole() == Roles.LEADER)
            rolesButtons[1].setBackground(getResources().getDrawable(R.drawable.red_tablet1));
        rolesButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getRole() != Roles.LEADER) {
                    for (int i = 0; i < 3; i++) {
                        rolesButtons[i].setBackground(getResources().getDrawable(R.drawable.blue_tablet1));
                    }
                }
                //rolesButtons[1].setBackground(getResources().getDrawable(R.drawable.red_tablet1));
                player.setRole(Roles.LEADER);
                sendGameState();

            }
        });

        if (player.getRole() == Roles.USUAL_PLAYER)
            rolesButtons[2].setBackground(getResources().getDrawable(R.drawable.red_tablet1));
        rolesButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getRole() != Roles.USUAL_PLAYER) {
                    for (int i = 0; i < 3; i++) {
                        rolesButtons[i].setBackground(getResources().getDrawable(R.drawable.blue_tablet1));
                    }
                }
                //rolesButtons[2].setBackground(getResources().getDrawable(R.drawable.red_tablet1));
                player.setRole(Roles.USUAL_PLAYER);
                sendGameState();

            }
        });

        TextView playersCountInLobby = findViewById(R.id.players_count_in_lobby);
        playersCountInLobby.setText(getResources().getString(R.string.players_count_in_lobby) + " " +
                game.getPlayers().size() + "/" + game.getGameSettings().getPlayersCount());

        TextView playBegin = findViewById(R.id.play_begin_button);
        Switch isAutorun = findViewById(R.id.autorun_game_switch);
        isAutorun.setChecked(game.getGameSettings().isAutorun());

        if (!game.getGameSettings().isAutorun() && game.getPlayers().get(0).equals(player))
            playBegin.setVisibility(View.VISIBLE);
        else playBegin.setVisibility(View.INVISIBLE);

        if (!game.getPlayers().get(0).equals(player)) {
            playBegin.setVisibility(View.INVISIBLE);
            isAutorun.setVisibility(View.INVISIBLE);
        }

        playBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //начало игры (с диалоговым окном - подтверждением, если игроков меньше, чем в настройках)
                if (game.getPlayers().size() == game.getGameSettings().getPlayersCount()) {
                    game.setGameStatus(Game.IN_GAME);
                    sendGameState();
                } else if (game.getPlayers().size() < game.getGameSettings().getPlayersCount()) {
                    drawIfLessPlayersDialog();
                }
            }
        });

        isAutorun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isCreatorOfGame()) {
                    if (isChecked) {
                        game.getGameSettings().setAutorun(true);
                        game.checkIsTimeToBegin();
                        //playBegin.setVisibility(View.INVISIBLE);
                        sendGameState();
                    } else {
                        game.getGameSettings().setAutorun(false);
                        //playBegin.setVisibility(View.VISIBLE);
                        sendGameState();
                    }
                }
            }
        });
    }

    private void drawIfLessPlayersDialog() {
        Dialog lessPlayers = new Dialog(this);
        lessPlayers.setContentView(R.layout.dialog_confirm);
        lessPlayers.setCancelable(true);
        lessPlayers.show();

        TextView header = lessPlayers.findViewById(R.id.header);
        TextView text = lessPlayers.findViewById(R.id.text);
        ImageView yes = lessPlayers.findViewById(R.id.yes);
        ImageView no = lessPlayers.findViewById(R.id.no);

        header.setText(R.string.warning);
        text.setText(R.string.less_players_than_in_game_settings);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(game.getPlayers().size() < GameSettings.MIN_PLAYERS_COUNT) {
                    String textStr = "Для начала игры нужно по крайней мере " + GameSettings.MIN_PLAYERS_COUNT + " игрока";
                    Toast.makeText(getBaseContext(), textStr, Toast.LENGTH_SHORT).show();
                    lessPlayers.dismiss();
                }
                else {*/
                game.setGameStatus(Game.IN_GAME);
                sendGameState();
                lessPlayers.dismiss();
                //}
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lessPlayers.dismiss();
            }
        });

        lessPlayers.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                lessPlayers.dismiss();
            }
        });

    }

    public boolean isCreatorOfGame() {
        return game.getPlayers().get(0).equals(player);
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void onBackPressed() {
        game.getPlayers().remove(game.getPlayerById(player.getId()));
        dr.removeEventListener(vel);
        /*if(game.getPlayers().size() == 0) {
            GeneralFunctions.deleteGame(game.getGameCode());
        }*/
        //sendGameState();
        Intent intent = new Intent(LobbyActivity.this, GameModeActivity.class);
        startActivity(intent);
        finish();
    }
}