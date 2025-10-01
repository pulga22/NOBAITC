package me.julionxn.nobaitc.util;

public class LetterHelper {

    private static final String[] letters = new String[]{
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
    };

    public static String getLetter(int index){
        return letters[index % letters.length];
    }

}
