package com.example.myapp.model;

import java.io.Serializable;
import java.util.Objects;

public class Player implements Serializable {
    public static final int TEAM_NOT_SELECTED = 0;
    public static final int RED_TEAM_PLAYER = 1;
    public static final int BLUE_TEAM_PLAYER = 2;
    public static final int BOTH_TEAMS_PLAYER = 3; //2 устройства

    private String name = "";
    private String id;
    private String email;
    private int role;
    private int teamOfPlayer;

    public Player() {}

    public Player(int role) {
        this.role = role;
    }

    public Player(String name, String id, String email) {
        this.name = name;
        this.id = id;
        this.email = email;
        role = Roles.USUAL_PLAYER;
        teamOfPlayer = TEAM_NOT_SELECTED;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getTeamOfPlayer() {
        return teamOfPlayer;
    }

    public void setTeamOfPlayer(int teamOfPlayer) {
        this.teamOfPlayer = teamOfPlayer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean canClick(GameSettings gameSettings, boolean teamMove, boolean isTimeForVoting, boolean isPlayerSelectedForVoting) {
        boolean result = false;
        if(teamOfPlayer == BOTH_TEAMS_PLAYER || (teamOfPlayer == RED_TEAM_PLAYER && teamMove == Game.RED_TEAM) ||
                (teamOfPlayer == BLUE_TEAM_PLAYER && teamMove == Game.BLUE_TEAM)) {
            if (role == Roles.USUAL_PLAYER && gameSettings.getDevicesType() == GameSettings.TWO_DEVICES)
                result = true;
            else if (role == Roles.USUAL_PLAYER && gameSettings.getWordSelectionType() == GameSettings.ANYONE_CAN_CLICK)
                result = true;
            else if (role == Roles.USUAL_PLAYER && gameSettings.getWordSelectionType() == GameSettings.TEAM_VOTE && isTimeForVoting)
                result = true;
            else if(role == Roles.LEADER && gameSettings.getWordSelectionType() == GameSettings.ONE_PLAYER_CAN_CLICK) {
                result = true;
            }
            else if(role == Roles.USUAL_PLAYER && gameSettings.getWordSelectionType() == GameSettings.RANDOM_PLAYER_CAN_CLICK && isPlayerSelectedForVoting) {
                return true;
            }
            // условие, выбран ли этот игрок рандомно для голосования
        }
        return result;
    }

    public boolean isCreator(Game game) {
        return game.getPlayers().get(0).equals(this);
    }

    public boolean canSelectTeam(Game game, boolean isItPlayerWhoTryToSelect) {
        boolean result = false;
        GameSettings gameSettings = game.getGameSettings();
        if(isItPlayerWhoTryToSelect) {
            if (gameSettings.getDevicesType() != GameSettings.TWO_DEVICES) {
                if(gameSettings.getTeamSelectionType() == GameSettings.FREE_TEAM_CHOOSE)
                    return true;
            }
        }
        if(game.getGameSettings().getTeamSelectionType() == GameSettings.TEAM_SELECTION_BY_CREATOR
                && isCreator(game)) {
            return true;
        }
        return false;
    }

    public void setRandomTeamOfPlayer() {
        teamOfPlayer = (int)(Math.random() * 2) + 1;
    }

    public boolean equals(Player player) {
        return id.equals(player.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
