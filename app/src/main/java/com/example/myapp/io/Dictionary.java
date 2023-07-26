package com.example.myapp.io;

import java.io.*;
import java.util.*;

public class Dictionary {
    private static ArrayList<ArrayList<String>> dictionary = new ArrayList<>();

    public static void load(String dictionaryFilename) {
        dictionary.clear();
        for (int i = 0; i < 30; i++) {
            dictionary.add(new ArrayList<>());
        }
        try {
            File file = new File(dictionaryFilename);
            Scanner scan = new Scanner(file);
            while (scan.hasNext()) {
                String temp = scan.next();
                dictionary.get(temp.length()).add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void loadDict(InputStream dictionaryIn) {
        dictionary.clear();
        for (int i = 0; i < 30; i++) {
            dictionary.add(new ArrayList<>());
        }
        Scanner scan = new Scanner(dictionaryIn);
        while (scan.hasNext()) {
            String temp = scan.next();
            dictionary.get(temp.length()).add(temp);
        }
    }

    /*public static String generateWord() {
        ArrayList<ArrayList<String>> dictionary = new ArrayList<>();
        Dictionary.load(dictionary, "languages/word_rus.txt");
        int length = (int)(Math.random()*10) + 3;
        int rand = (int)(Math.random()*dictionary.get(length).size());
        return dictionary.get(length).get(rand);
    }*/

    public static String generateWord() {
        if(dictionary.size() == 0) {
            System.out.println("SOMETHING BAD");
        }
        int length = (int) (Math.random() * 10) + 3;
        int rand = (int) (Math.random() * dictionary.get(length).size());
        return dictionary.get(length).get(rand);
    }

    public static String generateWord(ArrayList<ArrayList<String>> dict) {
        int length = (int) (Math.random() * 10) + 3;
        int rand = (int) (Math.random() * dict.get(length).size());
        return dict.get(length).get(rand);
    }
}