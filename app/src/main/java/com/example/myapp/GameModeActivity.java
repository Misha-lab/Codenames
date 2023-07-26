package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.model.Game;
import com.example.myapp.model.GameSettings;
import com.example.myapp.model.Player;
import com.example.myapp.model.Roles;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class GameModeActivity extends AppCompatActivity {
    public static final int SIGN_IN_CODE = 1;
    public static final String GAME_MODE_ATTRIBUTE_NAME = "game_mode";

    private int gameMode;
    private int codeOfGame;
    private int step = 1;
    private Game game;

    private Dialog dialog;
    private DatabaseReference dr;
    private ImageView backButton;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_CODE) {
            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                Toast.makeText(getBaseContext(), "Вы авторизованы:)", Toast.LENGTH_SHORT).show();
                showView();
            } else {
                Toast.makeText(getBaseContext(), "Вы не авторизованы!", Toast.LENGTH_SHORT).show();
                authUser(true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_game_mode);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        authUser(true);
        showView();

    }

    @Override
    public void onBackPressed() {}

    public void authUser(boolean isNeedCheckThatUserIsNull) {
        if(FirebaseAuth.getInstance().getCurrentUser() == null || !isNeedCheckThatUserIsNull) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        }
    }


    public void showView() {
        dialog = new Dialog(this);

        drawChangeUserButton();
        drawBackButton();

        TextView chooseMode = findViewById(R.id.choose_mode);
        TextView whichModeSelected = findViewById(R.id.which_mode_selected);

        TextView onlineGame = findViewById(R.id.online_game_button);
        TextView gameWithFriends = findViewById(R.id.friend_game_button);


        onlineGame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (step == 1) {
                        chooseMode.setText("Выберите способ подключения к игре");
                        chooseMode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        onlineGame.setText("Создать игру");
                        gameWithFriends.setText("Перейти к списку игр");
                        gameWithFriends.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                        int dpAsPixels = GeneralFunctions.getDpAsPixels(GameModeActivity.this, 15);
                        gameWithFriends.setPadding(0,dpAsPixels,0,0);

                        step = 2;
                        gameMode = GameSettings.ONLINE_GAME;
                        whichModeSelected.setText(getString(R.string.game_online));
                        whichModeSelected.setVisibility(View.VISIBLE);

                        backButton.setVisibility(View.VISIBLE);
                    } else if (step == 2) {
                        Intent intent = new Intent(GameModeActivity.this, GameSettingsActivity.class);
                        intent.putExtra(GAME_MODE_ATTRIBUTE_NAME, gameMode);
                        startActivity(intent);
                        finish();
                    }
                }
                return true;
            }
        });


        gameWithFriends.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (step == 1) {
                        chooseMode.setText("Выберите способ подключения к игре");
                        chooseMode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        onlineGame.setText("Создать игру");
                        gameWithFriends.setText("Присоединиться к существующей игре");
                        gameWithFriends.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                        int dpAsPixels = GeneralFunctions.getDpAsPixels(GameModeActivity.this, 15);
                        gameWithFriends.setPadding(0,dpAsPixels,0,0);

                        step = 2;
                        gameMode = GameSettings.GAME_WITH_FRIENDS;
                        whichModeSelected.setText(getString(R.string.game_with_friends));
                        whichModeSelected.setVisibility(View.VISIBLE);

                        backButton.setVisibility(View.VISIBLE);


                    } else if (step == 2) {
                        if(gameMode == GameSettings.GAME_WITH_FRIENDS) {
                            dialog.setContentView(R.layout.enter_code_layout);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.setCancelable(true);
                            dialog.show();

                            EditText code = dialog.findViewById(R.id.code_input);
                            code.setInputType(InputType.TYPE_CLASS_NUMBER);

                            TextView enter = dialog.findViewById(R.id.join_to_game);
                            enter.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                        enter.setBackground(getDrawable(R.color.greenCB));
                                    }
                                    if (event.getAction() == MotionEvent.ACTION_UP) {
                                        enter.setBackground(getDrawable(R.color.blackCB));
                                        String codeStr = code.getText().toString();
                                        if (!codeStr.equals("")) {
                                            try {
                                                codeOfGame = Integer.parseInt(code.getText().toString());
                                                setUpdater();
                                            } catch (NumberFormatException nfe) {
                                                Toast.makeText(getBaseContext(), "Неправильный ввод!", Toast.LENGTH_SHORT).show();
                                                codeOfGame = 0;
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
                        else {
                            Intent intent = new Intent(GameModeActivity.this, GameListActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
                return true;
            }
        });

    }

    private void drawChangeUserButton() {
        ImageView changeUser = findViewById(R.id.change_user_button);
        changeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FirebaseAuth.getInstance().signOut();
                authUser(false);
            }
        });
    }

    private void drawBackButton() {
        backButton = findViewById(R.id.back_button_in_mode_activity);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                step = 1;
                Intent intent = new Intent(GameModeActivity.this, GameModeActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });
    }

    private void setUpdater() {
        dr = GeneralFunctions.getReferenceToGame(codeOfGame).child("game");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(codeOfGame != 0) {
                    game = snapshot.getValue(Game.class);
                    if (game != null) {
                        if(game.getGameSettings().getGameMode() == gameMode) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            assert user != null;
                            Player player = new Player(user.getDisplayName(), user.getUid(), user.getEmail());
                            if (game.getGameSettings().getDevicesType() == GameSettings.TWO_DEVICES) {
                                player.setRole(Roles.USUAL_PLAYER);
                                player.setTeamOfPlayer(Player.BOTH_TEAMS_PLAYER);
                            }

                            Intent intent = null;
                            int result = game.acceptToGame(player);
                            if(game.getGameStatus() != Game.GAME_NOT_BEGIN) {
                                if(result == Game.NO_PLACES_ERROR) {
                                    player.setRole(Roles.SPECTATOR);
                                    intent = new Intent(GameModeActivity.this, GameActivity.class);
                                }
                                else if(result == Game.RECONNECT) {
                                    intent = new Intent(GameModeActivity.this, GameActivity.class);
                                }
                                else {
                                    intent = new Intent(GameModeActivity.this, LobbyActivity.class);
                                }
                            }
                            else {
                                if (result == Game.NO_PLACES_ERROR) {
                                    Toast.makeText(getBaseContext(), "В лобби нет мест!", Toast.LENGTH_SHORT).show();
                                } else if (result == Game.RECONNECT) {
                                    intent = new Intent(GameModeActivity.this, LobbyActivity.class);
                                } else {
                                    intent = new Intent(GameModeActivity.this, LobbyActivity.class);
                                }
                            }

                            assert intent != null;
                            intent.putExtra("game", game);

                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(getBaseContext(), "Выбран неправильный режим игры!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getBaseContext(), "Игры с таким кодом не существует!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}