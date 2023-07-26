package com.example.myapp.model;

import java.io.Serializable;

public class Card implements Serializable {
    private String word;
    private boolean isOpenedWord;
    private int teamOfCell;

    private int rowInBoard;
    private int columnInBoard;

    private int votesInVoteCount;
    private boolean canVote = true;

    public Card() {}

    public Card(String newWord, int rowInBoard, int columnInBoard) {
        word = newWord;
        isOpenedWord = false;
        teamOfCell = TemplateBoard.TEAM_NOT_DEFINED;

        this.rowInBoard = rowInBoard;
        this.columnInBoard = columnInBoard;
    }

    public void openCard() {
        isOpenedWord = true;
        canVote = false;
    }

    public boolean isCanVote() {
        return canVote;
    }

    public void setCanVote(boolean canVote) {
        this.canVote = canVote;
    }

    public String getWord() {
        return word;
    }

    public int getRowInBoard() {
        return rowInBoard;
    }

    public int getColumnInBoard() {
        return columnInBoard;
    }

    public boolean isOpenedWord() {
        return isOpenedWord;
    }

    public void setOpenedWord(boolean isOpenedWord) {
        this.isOpenedWord = isOpenedWord;
    }

    public void setTeamOfCell(int teamOfCell) {
        this.teamOfCell = teamOfCell;
    }

    public int getTeamOfCell() {
        return teamOfCell;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setRowInBoard(int rowInBoard) {
        this.rowInBoard = rowInBoard;
    }

    public void setColumnInBoard(int columnInBoard) {
        this.columnInBoard = columnInBoard;
    }

    public int getVotesInVoteCount() {
        return votesInVoteCount;
    }

    public void setVotesInVoteCount(int votesInVoteCount) {
        this.votesInVoteCount = votesInVoteCount;
    }

    public void registerVote() {
        votesInVoteCount++;
    }

    public void removeVotes() {
        votesInVoteCount = 0;
    }
}
