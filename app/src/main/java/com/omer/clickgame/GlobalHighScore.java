package com.omer.clickgame;


/**
 * Created by bimstajyer1 on 8.02.2018.
 */

public class GlobalHighScore {
    String name;
    int score;

    public GlobalHighScore(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
