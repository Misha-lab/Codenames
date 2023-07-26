package com.example.myapp.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TemplateBoard implements Serializable {
    public static final int TEAM_NOT_DEFINED = 0;
    public static final int RED_CELL = 1;
    public static final int BLUE_CELL = 2;
    public static final int BLACK_CELL = 3;
    public static final int EMPTY_CELL = 4;

    private ArrayList<ArrayList<Integer>> board;

    public TemplateBoard() {
        board = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            board.add(new ArrayList<>(5));
        }
    }

    public ArrayList<ArrayList<Integer>> getBoard() {
        return board;
    }

    public void generateBoard(int red_words_count, int blue_words_count, int empty_words_count,
                              int black_words_count) {

        board = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ArrayList<Integer> temp = new ArrayList<>(5);
            for (int j = 0; j < 5; j++) {
                temp.add(0);
            }
            board.add(temp);

        }

        Point[] points = new Point[25];

        for (int i = 0; i < 25; i++) {
            points[i] = new Point(-1,-1);
        }

        for (int i = 0; i < 25; i++) {
            boolean flag = false;
            while(!flag) {
                boolean isUnique = true;
                int randX = (int) (Math.random() * 5);
                int randY = (int) (Math.random() * 5);
                for (int j = 0; j < i; j++) {
                    if (points[j].x == randX && points[j].y == randY) {
                        isUnique = false;
                        break;
                    }
                }
                if (isUnique) {
                    points[i] = new Point(randX, randY);
                    flag = true;
                }
            }
        }
        boolean flag = false;
        for (int i = 0; i < 25; i++) {
            while(!flag) {
                int rand = (int)(Math.random()*4) + 1;
                if(rand == EMPTY_CELL) {
                    if (empty_words_count > 0) {
                        board.get(points[i].x).set(points[i].y, EMPTY_CELL);
                        empty_words_count--;
                        flag = true;
                    }
                }
                if(rand == RED_CELL) {
                    if(red_words_count > 0) {
                        board.get(points[i].x).set(points[i].y, RED_CELL);
                        red_words_count--;
                        flag = true;
                    }
                }
                if(rand == BLUE_CELL) {
                    if(blue_words_count > 0) {
                        board.get(points[i].x).set(points[i].y, BLUE_CELL);
                        blue_words_count--;
                        flag = true;
                    }
                }
                if(rand == BLACK_CELL) {
                    if(black_words_count > 0) {
                        board.get(points[i].x).set(points[i].y, BLACK_CELL);
                        black_words_count--;
                        flag = true;
                    }
                }
            }
            flag = false;
        }
    }

    public int getValueAt(int x, int y) {
        return board.get(x).get(y);
    }

    public void printBoardToConsole() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(board.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }
}
