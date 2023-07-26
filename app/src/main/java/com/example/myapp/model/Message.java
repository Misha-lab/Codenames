package com.example.myapp.model;

import java.io.Serializable;

public class Message implements Serializable {
    private Player player;
    private String userName;
    private String text;

    public Message() {}

    public Message(Player player, String text) {
        this.player = player;
        this.userName = player.getName();
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
