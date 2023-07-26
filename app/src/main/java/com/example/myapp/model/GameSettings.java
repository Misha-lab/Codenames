package com.example.myapp.model;

import java.io.Serializable;

public class GameSettings implements Serializable {
    public static final int MAX_PLAYERS_COUNT = 10;
    public static final int MIN_PLAYERS_COUNT = 4;

    public static final int PARAMETER_NOT_DEFINED = 0;
    public static final int TWO_DEVICES = 2;
    public static final int MANY_DEVICES = 1;
    public static final int ANYONE_CAN_CLICK = 3;
    public static final int TEAM_VOTE = 4;
    public static final int ONE_PLAYER_CAN_CLICK = 5;
    public static final int RANDOM_PLAYER_CAN_CLICK = 6;
    public static final int FREE_TEAM_CHOOSE = 7;
    public static final int RANDOM_TEAM_SELECTION = 8;
    public static final int TEAM_SELECTION_BY_CREATOR = 9;
    public static final int ONLINE_GAME = -1;
    public static final int GAME_WITH_FRIENDS = -2;

    private int gameMode = PARAMETER_NOT_DEFINED;
    private int playersCount = PARAMETER_NOT_DEFINED;
    private int devicesType = PARAMETER_NOT_DEFINED;
    private int wordSelectionType = PARAMETER_NOT_DEFINED;
    private int teamSelectionType = PARAMETER_NOT_DEFINED;
    private boolean autorun = true;

    public GameSettings() {
        playersCount = MIN_PLAYERS_COUNT;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public int getDevicesType() {
        return devicesType;
    }

    public void setDevicesType(int devicesType) {
        this.devicesType = devicesType;
    }

    public int getWordSelectionType() {
        return wordSelectionType;
    }

    public void setWordSelectionType(int wordSelectionType) {
        this.wordSelectionType = wordSelectionType;
    }

    public int getTeamSelectionType() {
        return teamSelectionType;
    }

    public void setTeamSelectionType(int teamSelectionType) {
        this.teamSelectionType = teamSelectionType;
    }

    public int getPlayersCount() {
        return playersCount;
    }

    public void setPlayersCount(int playersCount) {
        this.playersCount = playersCount;
    }

    public void incrementPlayersCount() {
        playersCount++;
    }

    public void decrementPlayersCount() {
        playersCount--;
    }

    public boolean isAutorun() {
        return autorun;
    }

    public void setAutorun(boolean autorun) {
        this.autorun = autorun;
    }
}
