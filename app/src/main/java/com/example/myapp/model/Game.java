package com.example.myapp.model;

import android.widget.Toast;

import com.example.myapp.GeneralFunctions;
import com.example.myapp.io.Dictionary;
import com.google.android.gms.safetynet.SafetyNetApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.ErrorManager;

public class Game implements Serializable {

    public static final String waitingForClue = "*ожидается*";

    public static final int OK = 0;
    public static final int NO_PLACES_ERROR = 100;
    public static final int RECONNECT = 150;
    public static final int EXTRA_TRY = 200;
    public static final int TEAM_MOVE_CHANGED = 201;
    public static final int GAME_ALREADY_BEGINS = 222;
    public static final int TIME_TO_START_GAME = 250;

    public static final int GAME_NOT_BEGIN = 0;
    public static final boolean RED_TEAM = false;
    public static final boolean BLUE_TEAM = true;
    public static final int IN_GAME = 3;
    public static final int RED_WIN = 4;
    public static final int BLUE_WIN = 5;
    public static final int ON_BLACK_CELL = 6;

    private ArrayList<ArrayList<Card>> cards;
    private ArrayList<ArrayList<Integer>> whenOpenedIndexes;
    private TemplateBoard templateBoard;
    private int gameStatus;
    private int gameCode;

    private int redWordsCount;
    private int blueWordsCount;
    private int emptyWordsCount;
    private int blackWordsCount;

    private int wordsCountInClue;
    private int wordsCountInClueRemain;
    private boolean isNewClueGiven;
    private boolean isTimeForVoting;
    private String currentClue;
    private int messageID;
    private String randomPlayerId;

    private boolean isExtraTryUsed;

    private int redLosesCount = 0;
    private int blueLosesCount = 0;

    private boolean teamMove;
    private ArrayList<Player> players;
    private ArrayList<Player> spectators;
    private GameSettings gameSettings;

    public Game() {}

    public Game(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
        Random random = new Random();
        gameCode = random.nextInt(8999) + 1000; //не забыть добавить, чтобы генерировался уникальный код

        /*boolean isUnique = false;
        do {
            GameList gameList = new GameList();
            gameCode = random.nextInt(8999) + 1000; //не забыть добавить, чтобы генерировался уникальный код
            if(!gameList.getGameCodes().contains(gameCode))
                isUnique = true;
        }
        while(!isUnique);*/

        gameStatus = GAME_NOT_BEGIN;
        players = new ArrayList<>();
        spectators = new ArrayList<>();
    }

    public boolean isNewPlayer(Player player) {
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).equals(player))
                return false;
        }
        return true;
    }

    public boolean isNewSpectator(Player spectator) {
        boolean result = isNewPlayer(spectator);
        for (int i = 0; i < spectators.size(); i++) {
            if(spectators.get(i).equals(spectator))
                return false;
        }
        return result;
    }

    public int acceptToGame(Player player) {
        if(isNewPlayer(player)) {
            if(gameStatus == GAME_NOT_BEGIN) {
                if (players.size() < gameSettings.getPlayersCount()) {
                    players.add(player);
                    if(gameSettings.getTeamSelectionType() == GameSettings.RANDOM_TEAM_SELECTION)
                        player.setRandomTeamOfPlayer();

                    if(gameSettings.isAutorun()) {
                        boolean isGameBeginning = checkIsTimeToBegin();
                        if(isGameBeginning)
                            return TIME_TO_START_GAME;
                    }
                } else {
                    if(spectators == null)
                        spectators = new ArrayList<>();
                    if(isNewSpectator(player))
                        spectators.add(player);
                    return NO_PLACES_ERROR;
                }
            }
            else {
                if(spectators == null)
                    spectators = new ArrayList<>();
                spectators.add(player);
                return GAME_ALREADY_BEGINS;
            }
        }
        else {
            return RECONNECT;
        }
        return OK;
    }

    public void startTheGame() {
        teamMove = ((int) (Math.random() * 2)) == 0 ? RED_TEAM : BLUE_TEAM;
        redWordsCount = 8;
        blueWordsCount = 8;
        emptyWordsCount = 7;
        blackWordsCount = 1;

        if(!teamMove)
            redWordsCount = 9;
        else
            blueWordsCount = 9;
        isNewClueGiven = false;

        templateBoard = new TemplateBoard();
        templateBoard.generateBoard(redWordsCount, blueWordsCount, emptyWordsCount, blackWordsCount);

        cards = new ArrayList<>();
        whenOpenedIndexes = new ArrayList<>();
        ArrayList<String> words = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            cards.add(new ArrayList<>());
            whenOpenedIndexes.add(new ArrayList<>());
            for (int j = 0; j < 5; j++) {
                cards.get(i).add(null);

                boolean isUniqueWord = false;
                String word;
                do {
                    word = Dictionary.generateWord();
                    if(!words.contains(word)) {
                        cards.get(i).set(j, new Card(word, i, j));
                        words.add(word);
                        isUniqueWord = true;
                    }
                }
                while(!isUniqueWord);

                cards.get(i).get(j).setTeamOfCell(templateBoard.getValueAt(i,j));

                int team = templateBoard.getValueAt(i, j);
                if (Math.random() < 0.5)
                    whenOpenedIndexes.get(i).add(team - 1);
                else
                    whenOpenedIndexes.get(i).add(team + 3);
            }
        }
        currentClue = waitingForClue;
        //gameStatus = IN_GAME;
    }

    public void move(int x, int y, boolean isExtra) {
        cards.get(x).get(y).openCard();
        if (cards.get(x).get(y).getTeamOfCell() == TemplateBoard.RED_CELL) {
            redWordsCount--;
            if (teamMove == BLUE_TEAM) {
                if(!isExtra)
                    changeTeamMove();
                blueLosesCount += wordsCountInClueRemain;
            } else {
                wordsCountInClueRemain--;
                if(isExtra) {
                    redLosesCount--;
                }
            }
        } else if (cards.get(x).get(y).getTeamOfCell() == TemplateBoard.BLUE_CELL) {
            blueWordsCount--;
            if (teamMove == RED_TEAM) {
                if(!isExtra)
                    changeTeamMove();
                redLosesCount += wordsCountInClueRemain;
            } else {
                wordsCountInClueRemain--;
                if(isExtra) {
                    blueLosesCount--;
                }
            }
        } else if (cards.get(x).get(y).getTeamOfCell() == TemplateBoard.BLACK_CELL) {
            blackWordsCount--;
        } else if (cards.get(x).get(y).getTeamOfCell() == TemplateBoard.EMPTY_CELL) {
            emptyWordsCount--;
            if (teamMove == RED_TEAM)
                redLosesCount += wordsCountInClueRemain;
            else if (teamMove == BLUE_TEAM)
                blueLosesCount += wordsCountInClueRemain;
            if(!isExtra)
                changeTeamMove();
        }
    }

    public void makeMove(int x, int y) {
        boolean prevTeamMove = teamMove;
        if(gameSettings.getWordSelectionType() != GameSettings.TEAM_VOTE) {
            boolean isIdChanged = false;
            if (!cards.get(x).get(y).isOpenedWord() && gameStatus == Game.IN_GAME) {

                int teamLoses = teamMove ? blueLosesCount : redLosesCount;
                if (wordsCountInClueRemain > 0) { //проверка на то, остались ли попытки (основные ИЛИ дополнительная)
                    move(x, y, false);

                    if (wordsCountInClueRemain <= 0) {
                        if (teamLoses <= 0 || isExtraTryUsed) {
                            changeTeamMove();
                        } else {
                            messageID = EXTRA_TRY;
                            isIdChanged = true;
                        }
                    }
                } else {
                    if (teamLoses > 0 && !isExtraTryUsed) {
                        move(x, y, true);
                    }
                    changeTeamMove();
                }
            }
            if (!isIdChanged) {
                if(prevTeamMove != teamMove) {
                    messageID = TEAM_MOVE_CHANGED;
                }
                else
                    messageID = OK;
            }
            updateRandomPlayerId();
            checkWin();
        }
        else {
            cards.get(x).get(y).registerVote();
        }
    }

    private boolean haveExtraTry() {
        int teamLoses = teamMove ? blueLosesCount : redLosesCount;
        return teamLoses > 0 && !isExtraTryUsed;
    }

    public boolean checkIsTimeToBegin() {
        if (players.size() == gameSettings.getPlayersCount() && gameStatus != IN_GAME) {
            gameStatus = IN_GAME;
            updateRandomPlayerId();
            return true;
        }
        return false;
    }

    public void timeOfVotingUpdate() {
        if(gameSettings.getWordSelectionType() == GameSettings.TEAM_VOTE) {
            isTimeForVoting = isNewClueGiven;
        }
        else isTimeForVoting = false;
    }

    public void updateRandomPlayerId() {
        if(gameSettings.getWordSelectionType() == GameSettings.RANDOM_PLAYER_CAN_CLICK) {
            ArrayList<Integer> playersFromTeamIndexes = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                if (GeneralFunctions.isPlayerFromTeamThatMove(players.get(i).getTeamOfPlayer(), teamMove)
                        && players.get(i).getRole() != Roles.CAPTAIN) {
                    playersFromTeamIndexes.add(i);
                }
            }
            int ind = (int) (Math.random() * playersFromTeamIndexes.size());
            randomPlayerId = players.get(playersFromTeamIndexes.get(ind)).getId();
        }
    }

    public void updateClueAndCount(String newClue, int newCount) {
        currentClue = newClue;
        wordsCountInClue = newCount;
        wordsCountInClueRemain = wordsCountInClue;
        isNewClueGiven = true;
        timeOfVotingUpdate();
        updateRandomPlayerId();
    }

    private void changeTeamMove() {
        teamMove = !teamMove;
        isExtraTryUsed = false;
        isNewClueGiven = false;
        currentClue = waitingForClue;
        //timeOfVotingUpdate();
    }

    public void checkWin() {
        if(gameStatus == IN_GAME) {
            if (blackWordsCount == 0) {
                gameStatus = ON_BLACK_CELL;
            }
            if (redWordsCount == 0) {
                gameStatus = RED_WIN;
            }
            if (blueWordsCount == 0) {
                gameStatus = BLUE_WIN;
            }
        }
    }

    public Player getPlayerById(String id) {
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getId().equals(id))
                return players.get(i);
        }
        if(spectators != null) {
            for (int i = 0; i < spectators.size(); i++) {
                if (spectators.get(i).getId().equals(id))
                    return spectators.get(i);
            }
        }
        return null;
    }

    public boolean isTimeForVoting() {
        if(gameSettings.getWordSelectionType() == GameSettings.TEAM_VOTE) {
            return isTimeForVoting;
        }
        return false;
    }

    public void setTimeForVoting(boolean timeForVoting) {
        isTimeForVoting = timeForVoting;
    }

    public String getRandomPlayerId() {
        return randomPlayerId;
    }

    public void setRandomPlayerId(String randomPlayerId) {
        this.randomPlayerId = randomPlayerId;
    }

    public ArrayList<ArrayList<Card>> getCards() {
        return cards;
    }

    public void setCards(ArrayList<ArrayList<Card>> cards) {
        this.cards = cards;
    }

    public ArrayList<ArrayList<Integer>> getWhenOpenedIndexes() {
        return whenOpenedIndexes;
    }

    public void setWhenOpenedIndexes(ArrayList<ArrayList<Integer>> whenOpenedIndexes) {
        this.whenOpenedIndexes = whenOpenedIndexes;
    }

    public TemplateBoard getTemplateBoard() {
        return templateBoard;
    }

    public void setTemplateBoard(TemplateBoard templateBoard) {
        this.templateBoard = templateBoard;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getGameCode() {
        return gameCode;
    }

    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }

    public int getRedWordsCount() {
        return redWordsCount;
    }

    public void setRedWordsCount(int redWordsCount) {
        this.redWordsCount = redWordsCount;
    }

    public int getBlueWordsCount() {
        return blueWordsCount;
    }

    public void setBlueWordsCount(int blueWordsCount) {
        this.blueWordsCount = blueWordsCount;
    }

    public int getEmptyWordsCount() {
        return emptyWordsCount;
    }

    public void setEmptyWordsCount(int emptyWordsCount) {
        this.emptyWordsCount = emptyWordsCount;
    }

    public int getBlackWordsCount() {
        return blackWordsCount;
    }

    public void setBlackWordsCount(int blackWordsCount) {
        this.blackWordsCount = blackWordsCount;
    }

    public int getWordsCountInClue() {
        return wordsCountInClue;
    }

    public void setWordsCountInClue(int wordsCountInClue) {
        this.wordsCountInClue = wordsCountInClue;
    }

    public int getWordsCountInClueRemain() {
        return wordsCountInClueRemain;
    }

    public void setWordsCountInClueRemain(int wordsCountInClueRemain) {
        this.wordsCountInClueRemain = wordsCountInClueRemain;
    }

    public boolean isNewClueGiven() {
        return isNewClueGiven;
    }

    public void setNewClueGiven(boolean newClueGiven) {
        isNewClueGiven = newClueGiven;
    }

    public int getRedLosesCount() {
        return redLosesCount;
    }

    public void setRedLosesCount(int redLosesCount) {
        this.redLosesCount = redLosesCount;
    }

    public int getBlueLosesCount() {
        return blueLosesCount;
    }

    public void setBlueLosesCount(int blueLosesCount) {
        this.blueLosesCount = blueLosesCount;
    }

    public boolean isTeamMove() {
        return teamMove;
    }

    public void setTeamMove(boolean teamMove) {
        this.teamMove = teamMove;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public String getCurrentClue() {
        return currentClue;
    }

    public void setCurrentClue(String currentClue) {
        this.currentClue = currentClue;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public boolean isExtraTryUsed() {
        return isExtraTryUsed;
    }

    public void setExtraTryUsed(boolean extraTryUsed) {
        isExtraTryUsed = extraTryUsed;
    }

    public ArrayList<Player> getSpectators() {
        return spectators;
    }

    public void setSpectators(ArrayList<Player> spectators) {
        this.spectators = spectators;
    }
}
