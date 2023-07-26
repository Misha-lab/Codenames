package com.example.myapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.model.Game;
import com.example.myapp.model.GameSettings;
import com.example.myapp.model.Player;
import com.example.myapp.model.Roles;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;

public class GeneralFunctions {

    public static final String GAME_LIST_REFERENCE_PATH = "game_list";
    public static final String GAME_REFERENCE_PATH = "game";

    public static void joinToGame(AppCompatActivity activity, Game game) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        Player player = new Player(user.getDisplayName(), user.getUid(), user.getEmail());
        if (game.getGameSettings().getDevicesType() == GameSettings.TWO_DEVICES) {
            player.setRole(Roles.USUAL_PLAYER);
            player.setTeamOfPlayer(Player.BOTH_TEAMS_PLAYER);
        }

        if (activity instanceof LobbyActivity) {
            ((LobbyActivity) activity).removeListener();
        } else if (activity instanceof GameListActivity) {
            ((GameListActivity) activity).removeListener();
        } else if (activity instanceof GameActivity) {
            ((GameActivity) activity).removeListener();
        }

        Intent intent = null;
        int result = game.acceptToGame(player);
        if (game.getGameStatus() != Game.GAME_NOT_BEGIN) {
            if (result == Game.NO_PLACES_ERROR || result == Game.GAME_ALREADY_BEGINS) {
                player.setRole(Roles.SPECTATOR);
                intent = new Intent(activity, GameActivity.class);
            } else if (result == Game.RECONNECT) {
                //player = game.getPlayerById(player.getId());
                intent = new Intent(activity, GameActivity.class);
            } else {
                intent = new Intent(activity, LobbyActivity.class);
            }
        } else {
            if (result == Game.NO_PLACES_ERROR) {
                Toast.makeText(activity.getBaseContext(), "В лобби нет мест!", Toast.LENGTH_SHORT).show();
            } else if (result == Game.RECONNECT) {
                //player = game.getPlayerById(player.getId());
                intent = new Intent(activity, LobbyActivity.class);
            } else {
                intent = new Intent(activity, LobbyActivity.class);
            }
        }

        assert intent != null;
        intent.putExtra("game", game);
        //intent.putExtra("player", player);

        activity.startActivity(intent);
        activity.finish();
    }

    public static void showSettingsOfGameLayout(AppCompatActivity activity, Game game) {

        Dialog settings = new Dialog(activity);
        settings.setContentView(R.layout.show_game_settings_layout);
        settings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        settings.setCancelable(true);
        settings.show();

        TextView gameCode = settings.findViewById(R.id.game_code_in_settings_layout);
        TextView playersCount = settings.findViewById(R.id.players_count_in_settings_layout);
        TextView wordSelectionType = settings.findViewById(R.id.word_selection_type_in_settings_layout);
        TextView teamSelectionType = settings.findViewById(R.id.team_selection_type_in_settings_layout);

        String gameCodeStr = activity.getResources().getString(R.string.code_to_join_message) + " " + game.getGameCode();
        String playersCountStr = activity.getResources().getString(R.string.max_players_count_string)
                + ": " + game.getGameSettings().getPlayersCount();


        String wordSelectionTypeString = activity.getResources().getString(R.string.vote_type) + ": ";
        if (game.getGameSettings().getWordSelectionType() == GameSettings.ANYONE_CAN_CLICK) {
            wordSelectionTypeString += activity.getResources().getString(R.string.anyone_can_click);
        } else if (game.getGameSettings().getWordSelectionType() == GameSettings.TEAM_VOTE) {
            wordSelectionTypeString += activity.getResources().getString(R.string.all_vote);
        } else if (game.getGameSettings().getWordSelectionType() == GameSettings.ONE_PLAYER_CAN_CLICK) {
            wordSelectionTypeString += activity.getResources().getString(R.string.one_can_click);
        } else if (game.getGameSettings().getWordSelectionType() == GameSettings.RANDOM_PLAYER_CAN_CLICK) {
            wordSelectionTypeString += activity.getResources().getString(R.string.random_player_click);
        }

        String teamSelectionTypeString = activity.getResources().getString(R.string.team_selection) + ": ";
        if (game.getGameSettings().getTeamSelectionType() == GameSettings.FREE_TEAM_CHOOSE) {
            teamSelectionTypeString += activity.getResources().getString(R.string.free_team_choose);
        } else if (game.getGameSettings().getTeamSelectionType() == GameSettings.RANDOM_TEAM_SELECTION) {
            teamSelectionTypeString += activity.getResources().getString(R.string.random_team_selection);
        } else if (game.getGameSettings().getTeamSelectionType() == GameSettings.TEAM_SELECTION_BY_CREATOR) {
            teamSelectionTypeString += activity.getResources().getString(R.string.creator_distibutes_team);
        }

        gameCode.setText(gameCodeStr);
        playersCount.setText(playersCountStr);
        wordSelectionType.setText(wordSelectionTypeString);
        teamSelectionType.setText(teamSelectionTypeString);

        settings.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                settings.dismiss();
            }
        });

        RelativeLayout layout = settings.findViewById(R.id.show_game_settings_layout);
        if (activity instanceof GameListActivity) {
            layout.setBackground(activity.getResources().getDrawable(R.drawable.wooden_background_light3));
        } else if (activity instanceof GameActivity) {
            layout.setBackground(activity.getResources().getDrawable(R.drawable.wooden_background_brown5));
        }
    }

    public static int millisToSeconds(long millis) {
        return (int) (millis / 1000);
    }


    public static boolean isCreator(Game game, Player player) {
        return game.getPlayers().get(0).equals(player);
    }

    public static void drawPlayerTablet(AppCompatActivity activity, RelativeLayout playerIcon, Player player, boolean isCanChangeTeam) {
        playerIcon.setVisibility(View.VISIBLE);
        TextView playerName = playerIcon.findViewById(R.id.player_name);
        playerName.setText(player.getName());
        ImageView teamView = playerIcon.findViewById(R.id.team_ball_in_lobby);
        if (player.getTeamOfPlayer() == Player.RED_TEAM_PLAYER)
            teamView.setBackground(activity.getResources().getDrawable(R.drawable.red));
        else if (player.getTeamOfPlayer() == Player.BLUE_TEAM_PLAYER)
            teamView.setBackground(activity.getResources().getDrawable(R.drawable.blue));
        else if (player.getTeamOfPlayer() == Player.BOTH_TEAMS_PLAYER) {
            teamView.setBackground(activity.getResources().getDrawable(R.drawable.both_teams));
        }

        if (isCanChangeTeam) {
            teamView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activity instanceof LobbyActivity) {
                        LobbyActivity lobbyActivity = (LobbyActivity) activity;
                        if (player.getTeamOfPlayer() == Player.RED_TEAM_PLAYER) {
                            player.setTeamOfPlayer(Player.BLUE_TEAM_PLAYER);
                            //teamView.setBackground(getResources().getDrawable(R.drawable.blue));
                        } else if (player.getTeamOfPlayer() == Player.BLUE_TEAM_PLAYER) {
                            player.setTeamOfPlayer(Player.RED_TEAM_PLAYER);
                            //teamView.setBackground(getResources().getDrawable(R.drawable.red));
                        } else if (player.getTeamOfPlayer() == Player.TEAM_NOT_SELECTED) {
                            player.setRandomTeamOfPlayer();
                        }
                        lobbyActivity.sendGameState();
                    }
                }
            });
        }

        ImageView playerRole = playerIcon.findViewById(R.id.player_role);
        Drawable roleDrawable;
        if (player.getRole() == Roles.CAPTAIN)
            roleDrawable = activity.getResources().getDrawable(R.drawable.captain_sign);
        else if (player.getRole() == Roles.LEADER) {
            roleDrawable = activity.getResources().getDrawable(R.drawable.leader_sign);
        } else if (player.getRole() == Roles.USUAL_PLAYER) {
            roleDrawable = activity.getResources().getDrawable(R.drawable.usual_player_sign);
        } else if (player.getRole() == Roles.SPECTATOR) {
            roleDrawable = activity.getResources().getDrawable(R.drawable.spectator_sign);
        } else {
            roleDrawable = activity.getResources().getDrawable(R.drawable.close_button);
        }
        playerRole.setForeground(roleDrawable);
    }

    public static boolean isPlayerFromTeamThatMove(int teamOfPlayer, boolean teamMove) {
        if (teamOfPlayer == Player.BOTH_TEAMS_PLAYER) {
            return true;
        } else if (teamOfPlayer == Player.RED_TEAM_PLAYER && teamMove == Game.RED_TEAM) {
            return true;
        } else if (teamOfPlayer == Player.BLUE_TEAM_PLAYER && teamMove == Game.BLUE_TEAM) {
            return true;
        }
        return false;
    }

    public static DatabaseReference getReferenceToGame(int gameCode) {
        return FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference("game" + gameCode);
    }

    public static DatabaseReference getReferenceToGameList() {
        return FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference(GAME_LIST_REFERENCE_PATH);
    }

    public static int getDpAsPixels(AppCompatActivity activity, int valueInDp) {
        float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (valueInDp * scale + 0.5f);
    }

    public static void deleteGame(int gameCode) {
        FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference(GAME_LIST_REFERENCE_PATH).child("game" + gameCode).removeValue();
        FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference("game" + gameCode).removeValue();
    }

    public static InputStream openInputStream(AppCompatActivity activity, String filename) {
        try {
            return activity.getAssets().open(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
