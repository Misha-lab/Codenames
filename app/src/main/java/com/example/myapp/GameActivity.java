package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.model.Card;
import com.example.myapp.model.Game;
import com.example.myapp.model.GameSettings;
import com.example.myapp.model.Message;
import com.example.myapp.model.Player;
import com.example.myapp.model.Roles;
import com.example.myapp.model.TemplateBoard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private Game game;
    private int codeOfGame;
    private Player player;
    private String playerID = FirebaseAuth.getInstance().getUid();
    private DatabaseReference dr;
    private DatabaseReference chatDr;
    private Dialog dialog;

    private Dialog showRedTeam;
    private Dialog showBlueTeam;
    private ValueEventListener vel;
    private ValueEventListener chatVel;

    private static final int[] arrayOfPics = {R.drawable.source_field, R.drawable.red_cell, R.drawable.blue_cell,
            R.drawable.black_cell};

    private static final int[][] cells_ID = {{R.id.cell00, R.id.cell01, R.id.cell02, R.id.cell03, R.id.cell04},
            {R.id.cell10, R.id.cell11, R.id.cell12, R.id.cell13, R.id.cell14},
            {R.id.cell20, R.id.cell21, R.id.cell22, R.id.cell23, R.id.cell24},
            {R.id.cell30, R.id.cell31, R.id.cell32, R.id.cell33, R.id.cell34},
            {R.id.cell40, R.id.cell41, R.id.cell42, R.id.cell43, R.id.cell44}};

    private static final int[][] words_ID = {{R.id.word00, R.id.word01, R.id.word02, R.id.word03, R.id.word04},
            {R.id.word10, R.id.word11, R.id.word12, R.id.word13, R.id.word14},
            {R.id.word20, R.id.word21, R.id.word22, R.id.word23, R.id.word24},
            {R.id.word30, R.id.word31, R.id.word32, R.id.word33, R.id.word34},
            {R.id.word40, R.id.word41, R.id.word42, R.id.word43, R.id.word44}};

    private static final int[] persons_ID = {R.drawable.red_cell_people0, R.drawable.blue_cell_people0, R.drawable.black_cell_people0, R.drawable.empty_cell_people0,
            R.drawable.red_cell_people1, R.drawable.blue_cell_people1, R.drawable.black_cell_people0, R.drawable.empty_cell_people1};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_game);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        Bundle bundle = getIntent().getExtras();
        game = (Game) bundle.get("game");

        if (game != null) {
            codeOfGame = game.getGameCode();
            player = game.getPlayerById(playerID);
        }

        setUpdater();
        sendGameState();
        drawBackButton();
        drawSettingsButton();
        drawGameView();
    }

    @Override
    public void onBackPressed() {
    }

    private void drawGameView() {

        TemplateBoard templateBoard = game.getTemplateBoard();
        LayerDrawable layers = (LayerDrawable) getResources().getDrawable(R.drawable.template);
        dialog = new Dialog(this);
        showRedTeam = new Dialog(this);
        showBlueTeam = new Dialog(this);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int team = templateBoard.getValueAt(i, j);
                if (team != TemplateBoard.EMPTY_CELL) {
                    layers.setDrawableByLayerId(cells_ID[i][j], getResources().getDrawable(arrayOfPics[team]));
                }
                RelativeLayout cardLayout = findViewById(words_ID[i][j]);
                Card card = game.getCards().get(i).get(j);
                TextView word = cardLayout.findViewById(R.id.word);
                word.setText(card.getWord());

                int index = game.getWhenOpenedIndexes().get(i).get(j);
                if (card.isOpenedWord())
                    word.setForeground(getDrawable(persons_ID[index]));

                int x = i, y = j;
                word.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (game.getCards().get(x).get(y).isOpenedWord()) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                word.setForeground(getDrawable(R.drawable.transparent));
                            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                                word.setForeground(getDrawable(persons_ID[index]));
                            }
                        } else {
                            if (player.canClick(game.getGameSettings(), game.isTeamMove(), game.isTimeForVoting(), checkIsPlayerSelectedForVoting())) {
                                if (game.isNewClueGiven()) {
                                    if (event.getAction() == MotionEvent.ACTION_UP) {
                                        game.makeMove(x, y);
                                        sendGameState();
                                    }
                                }
                            }
                        }
                        return true;
                    }
                });
            }
        }
        ((ImageView) findViewById(R.id.templateBoard)).setImageDrawable(layers);

        ImageView xMoveBall = findViewById(R.id.x_move_ball);


        if (game.isTeamMove() == Game.RED_TEAM)
            xMoveBall.setBackground(getDrawable(R.drawable.red_team));
        else if (game.isTeamMove() == Game.BLUE_TEAM)
            xMoveBall.setBackground(getDrawable(R.drawable.blue_team));

        TextView firstTeam = findViewById(R.id.first_team_remain_cards);
        TextView secondTeam = findViewById(R.id.second_team_remain_cards);
        if(player.getTeamOfPlayer() == Player.RED_TEAM_PLAYER ||
                (player.getTeamOfPlayer() == Player.BOTH_TEAMS_PLAYER && game.isTeamMove() == Game.RED_TEAM)) {
            firstTeam.setBackground(getResources().getDrawable(R.drawable.red_circle));
            firstTeam.setText(game.getRedWordsCount() + "");

            secondTeam.setBackground(getResources().getDrawable(R.drawable.blue_circle));
            secondTeam.setText(game.getBlueWordsCount() + "");
        }
        else if (player.getTeamOfPlayer() == Player.BLUE_TEAM_PLAYER ||
                (player.getTeamOfPlayer() == Player.BOTH_TEAMS_PLAYER && game.isTeamMove() == Game.BLUE_TEAM)) {
            firstTeam.setBackground(getResources().getDrawable(R.drawable.blue_circle));
            firstTeam.setText(game.getBlueWordsCount() + "");

            secondTeam.setBackground(getResources().getDrawable(R.drawable.red_circle));
            secondTeam.setText(game.getRedWordsCount() + "");
        }

        EditText clueInput = findViewById(R.id.clue_input);
        EditText countOfWordsInput = findViewById(R.id.count_of_words_input);
        ImageView sendClue = findViewById(R.id.send_clue);
        if (game.isNewClueGiven()) {
            clueInput.setEnabled(false);
            countOfWordsInput.setEnabled(false);
            sendClue.setEnabled(false);

        } else {
            clueInput.setEnabled(true);
            countOfWordsInput.setEnabled(true);
            sendClue.setEnabled(true);
        }

        sendClue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (game.isTeamMove() == Game.RED_TEAM && player.getTeamOfPlayer() == Player.RED_TEAM_PLAYER ||
                            game.isTeamMove() == Game.BLUE_TEAM && player.getTeamOfPlayer() == Player.BLUE_TEAM_PLAYER ||
                            player.getTeamOfPlayer() == Player.BOTH_TEAMS_PLAYER) {
                        boolean isBadClue = false;
                        String clue = clueInput.getText().toString();
                        String lowerCaseClue = clue.toLowerCase();
                        int count = 0;
                        String countStr = countOfWordsInput.getText().toString();
                        if (lowerCaseClue.equals("") && countStr.equals("")) {
                            Toast.makeText(getBaseContext(), "Вы не ввели слово и их количество!", Toast.LENGTH_SHORT).show();
                        } else if (lowerCaseClue.equals("")) {
                            Toast.makeText(getBaseContext(), "Вы не ввели слово!", Toast.LENGTH_SHORT).show();
                        } else if (countStr.equals("")) {
                            Toast.makeText(getBaseContext(), "Вы не ввели количество слов!", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                count = Integer.parseInt(countStr);
                            } catch (NumberFormatException nfe) {
                                Toast.makeText(getBaseContext(), "Неправильный ввод!", Toast.LENGTH_SHORT).show();
                                count = 0;
                            }
                            if (!isBadClue) {
                                game.updateClueAndCount(lowerCaseClue, count);
                                sendGameState();
                                clueInput.getText().clear();
                                countOfWordsInput.getText().clear();
                            } else {
                                Toast.makeText(getBaseContext(), "Необходимо ввести лишь одно слово!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                return true;
            }
        });

        TextView curClue = findViewById(R.id.current_clue);
        String clueMessage = getResources().getString(R.string.current_clue) + " " + game.getCurrentClue();
        if (game.getMessageID() == Game.EXTRA_TRY) {
            clueMessage += " | доп. попытка";
        } else if (!game.getCurrentClue().equals(Game.waitingForClue)) {
            clueMessage += " | " + game.getWordsCountInClueRemain();
        }
        curClue.setText(clueMessage);


        LinearLayout captainPanel = findViewById(R.id.captain_panel_layout);
        if (player.getRole() == Roles.CAPTAIN) {
            captainPanel.setVisibility(View.VISIBLE);
        } else {
            captainPanel.setVisibility(View.INVISIBLE);
        }

        if (game.getGameStatus() != Game.IN_GAME) {
            int winner = game.getGameStatus();
            dialog.setContentView(R.layout.winner_layout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int[] pics = {R.drawable.blue_win0, R.drawable.blue_win1, R.drawable.blue_win2,
                    R.drawable.red_win0, R.drawable.red_win1, R.drawable.red_win2};
            ImageView winnerBack = dialog.findViewById(R.id.winner_back);
            int rand = (int) (Math.random() * 3); //берем случайную из трех подходящих картинок
            if (winner == Game.RED_WIN) {
                winnerBack.setBackground(getResources().getDrawable(pics[3 + rand]));
            } else if (winner == Game.BLUE_WIN) {
                winnerBack.setBackground(getResources().getDrawable(pics[rand]));
            } else if (winner == Game.ON_BLACK_CELL) {
                if (game.isTeamMove() == Game.RED_TEAM)
                    winnerBack.setBackground(getResources().getDrawable(pics[rand]));
                else if (game.isTeamMove() == Game.BLUE_TEAM)
                    winnerBack.setBackground(getResources().getDrawable(pics[3 + rand]));
            }
            dialog.setCancelable(true);
            dialog.show();

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    game.getPlayers().remove(game.getPlayerById(player.getId()));
                    if (game.getPlayers().size() == 0) {
                        GeneralFunctions.deleteGame(codeOfGame);
                    }
                    removeListener();

                    Intent intent = new Intent(GameActivity.this, GameModeActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        ImageView showRedTeamButton = findViewById(R.id.show_red_team_button);
        showRedTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeam(Player.RED_TEAM_PLAYER);
            }
        });

        ImageView showBlueTeamButton = findViewById(R.id.show_blue_team_button);
        showBlueTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeam(Player.BLUE_TEAM_PLAYER);
            }
        });

        RelativeLayout playerIcon = findViewById(R.id.this_player);
        GeneralFunctions.drawPlayerTablet(GameActivity.this, playerIcon, player, false);

        updateMessageToPlayerInGame();
        drawChat();
    }

    private void drawChat() {
        if (player.getRole() != Roles.CAPTAIN && player.getRole() != Roles.SPECTATOR) {
            if (player.getTeamOfPlayer() != Player.BOTH_TEAMS_PLAYER) {
                RelativeLayout chat = findViewById(R.id.chat_layout);
                chat.setVisibility(View.VISIBLE);

                TextView chatStr = chat.findViewById(R.id.chat_str);
                if(player.getTeamOfPlayer() == Player.RED_TEAM_PLAYER)
                    chatStr.setText("Чат красных");
                else if(player.getTeamOfPlayer() == Player.BLUE_TEAM_PLAYER)
                    chatStr.setText("Чат синих");

                ScrollView scroll = chat.findViewById(R.id.scroll);
                scroll.fullScroll(View.FOCUS_DOWN);

                if (chatDr == null) {
                    String chatChild = null;
                    if (player.getTeamOfPlayer() == Player.RED_TEAM_PLAYER)
                        chatChild = "chatRed";
                    else if (player.getTeamOfPlayer() == Player.BLUE_TEAM_PLAYER)
                        chatChild = "chatBlue";

                    if(chatChild != null) {
                        chatDr = GeneralFunctions.getReferenceToGame(game.getGameCode()).child(chatChild);
                        chatVel = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                LinearLayout messagesLayout = chat.findViewById(R.id.all_messages_layout);
                                messagesLayout.removeAllViews();
                                if(game.getMessageID() == Game.TEAM_MOVE_CHANGED) {
                                    clearChat();
                                    game.setMessageID(Game.OK);
                                }
                                else {
                                    ArrayList<Message> messages = new ArrayList<>();
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        messages.add(ds.getValue(Message.class));
                                    }
                                    for (int i = 0; i < messages.size(); i++) {
                                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                        RelativeLayout message = (RelativeLayout) inflater.inflate(R.layout.message_layout,
                                                (RelativeLayout) findViewById(R.id.message_layout), false);
                                        TextView name = message.findViewById(R.id.user);
                                        TextView text = message.findViewById(R.id.text);
                                        if (messages.get(i).getPlayer().getId().equals(player.getId()))
                                            name.setText("Вы: ");
                                        else name.setText(messages.get(i).getUserName() + ": ");
                                        text.setText(messages.get(i).getText());
                                        messagesLayout.addView(message);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };
                        chatDr.addValueEventListener(chatVel);
                    }
                }

                EditText inputMessage = chat.findViewById(R.id.message_input);
                ImageView sendMessage = chat.findViewById(R.id.send_message);
                sendMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String messageStr = inputMessage.getText().toString();
                        inputMessage.getText().clear();
                        if(!messageStr.equals("")) {
                            chatDr.push().setValue(new Message(player, messageStr));
                        }
                    }
                });
            }
        }
    }

    private void clearChat() {
        chatDr.setValue(null);
    }

    private void showTeam(int team) {
        int[] playersId = {R.id.player0, R.id.player1, R.id.player2, R.id.player3, R.id.player4};

        Dialog showTeam = new Dialog(this);
        showTeam.setContentView(R.layout.players_in_team);
        showTeam.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        showTeam.setCancelable(true);
        showTeam.show();

        TextView whatTeamText = showTeam.findViewById(R.id.what_team_text);
        if (team == Player.RED_TEAM_PLAYER) {
            whatTeamText.setText("Команда красных");
        }
        else if (team == Player.BLUE_TEAM_PLAYER) {
            whatTeamText.setText("Команда синих");
        }
        else {
            Toast.makeText(getBaseContext(), "Игроки играют за обе команды", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < playersId.length; i++) {
            RelativeLayout playerIcon = showTeam.findViewById(playersId[i]);
            playerIcon.setVisibility(View.INVISIBLE);
        }
        int playersInTeam = 0;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player curPlayer = game.getPlayers().get(i);
            if (curPlayer.getTeamOfPlayer() == team) {
                RelativeLayout playerIcon = showTeam.findViewById(playersId[playersInTeam]);
                GeneralFunctions.drawPlayerTablet(GameActivity.this, playerIcon, curPlayer, false);
                playersInTeam++;
            }
        }

        showTeam.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                showTeam.dismiss();
            }
        });
    }

    private void setUpdater() {
        if (dr == null) {
            dr = GeneralFunctions.getReferenceToGame(codeOfGame);
            vel = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    game = snapshot.getValue(Game.class);
                    assert game != null;
                    player = game.getPlayerById(playerID);
                    drawGameView();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            dr.child(GeneralFunctions.GAME_REFERENCE_PATH).addValueEventListener(vel);
        }
    }

    private void sendGameState() {
        if (game != null)
            dr.child(GeneralFunctions.GAME_REFERENCE_PATH).setValue(game);
        else dr.removeValue();
    }

    public void removeListener() {
        if(dr != null)
            dr.removeEventListener(vel);
        if(chatDr != null)
            chatDr.removeEventListener(chatVel);
    }

    private boolean checkIsPlayerSelectedForVoting() {
        if (game.getGameSettings().getWordSelectionType() == GameSettings.RANDOM_PLAYER_CAN_CLICK) {
            if (game.getRandomPlayerId() != null) {
                return game.getRandomPlayerId().equals(player.getId());
            }
        }
        return false;
    }

    private void updateMessageToPlayerInGame() {
        TextView message = findViewById(R.id.message_to_player_in_game);
        String messageStr = "";
        if (game.isNewClueGiven()) {
            if (GeneralFunctions.isPlayerFromTeamThatMove(player.getTeamOfPlayer(), game.isTeamMove())) {
                if (player.getRole() != Roles.CAPTAIN) {
                    if (game.getGameSettings().getWordSelectionType() == GameSettings.ANYONE_CAN_CLICK) {
                        messageStr = "Ваш ход! Совещайтесь и выбирайте относящиеся к слову-подсказке слова!";
                    } else if (game.getGameSettings().getWordSelectionType() == GameSettings.TEAM_VOTE) {
                        messageStr = "Ваш ход! Проголосуйте за слова, которые вы хотите выбрать!";
                    }
                } else {
                    if (game.getGameSettings().getWordSelectionType() == GameSettings.ANYONE_CAN_CLICK) {
                        messageStr = "Игроки вашей команды совещаются и выбирают слова...";
                    } else if (game.getGameSettings().getWordSelectionType() == GameSettings.TEAM_VOTE) {
                        messageStr = "Игроки вашей команды голосуют...";
                    }
                }
                if (game.getGameSettings().getWordSelectionType() == GameSettings.ONE_PLAYER_CAN_CLICK) {
                    if (player.getRole() != Roles.LEADER) {
                        messageStr = "Лидер вашей команды выбирает слова...";
                    } else {
                        messageStr = "Ваш ход! Вы лидер, выбирайте слова!";
                    }
                }
                if (game.getGameSettings().getWordSelectionType() == GameSettings.RANDOM_PLAYER_CAN_CLICK) {
                    if (checkIsPlayerSelectedForVoting()) {
                        messageStr = "Вы становитесь лидером на 1 ход! Выберите слово!";
                    } else {
                        messageStr = "Слово выбирает случайно выбранный игрок вашей команды - " + game.getPlayerById(game.getRandomPlayerId()).getName();
                    }
                }
            } else {
                if (game.getGameSettings().getWordSelectionType() == GameSettings.ANYONE_CAN_CLICK) {
                    messageStr = "Игроки команды соперника угадывают слова...";
                } else if (game.getGameSettings().getWordSelectionType() == GameSettings.TEAM_VOTE) {
                    messageStr = "Игроки команды соперника голосуют...";
                } else if (game.getGameSettings().getWordSelectionType() == GameSettings.ONE_PLAYER_CAN_CLICK) {
                    messageStr = "Лидер команды соперника выбирает слова...";
                } else if (game.getGameSettings().getWordSelectionType() == GameSettings.RANDOM_PLAYER_CAN_CLICK) {
                    messageStr = "Слово выбирает случайно выбранный игрок команды противника - " + game.getPlayerById(game.getRandomPlayerId()).getName();
                }
            }
        } else {
            if (GeneralFunctions.isPlayerFromTeamThatMove(player.getTeamOfPlayer(), game.isTeamMove())) {
                if (player.getTeamOfPlayer() == Player.BOTH_TEAMS_PLAYER) {
                    if (player.getRole() != Roles.CAPTAIN)
                        messageStr = "Ожидаем слово-подсказку от капитана...";
                    else messageStr = "Введите слово-подсказку!";
                } else {
                    if (player.getRole() != Roles.CAPTAIN)
                        messageStr = "Ожидаем слово-подсказку от капитана вашей команды...";
                    else messageStr = "Введите слово-подсказку!";
                }
            } else {
                messageStr = "Ожидаем слово-подсказку от капитана команды противника...";
            }
        }
        message.setText(messageStr);
    }

    private void drawBackButton() {
        ImageView backButton = findViewById(R.id.back_button_in_game_activity);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removeListener();
                Intent intent = new Intent(GameActivity.this, GameModeActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });
    }

    private void drawSettingsButton() {
        ImageView settingsButton = findViewById(R.id.show_game_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralFunctions.showSettingsOfGameLayout(GameActivity.this, game);
            }
        });
    }
}