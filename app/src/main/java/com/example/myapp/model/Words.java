//package com.company.model;
//
//import com.company.io.Dictionary;
//
//public class Words {
//    private Card[][] words;
//
//    public Words() {
//        words = new Card[5][5];
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 5; j++) {
//                words[i][j] = new Card(Dictionary.generateWord(), i, j);
//            }
//        }
//    }
//
//    public void setWords(Card[][] words) {
//        this.words = words;
//    }
//
//    public void setWordAt(Card word, int x, int y) {
//        words[x][y] = word;
//    }
//
//    public Card getWordAt(int x, int y) {
//        return words[x][y];
//    }
//}
