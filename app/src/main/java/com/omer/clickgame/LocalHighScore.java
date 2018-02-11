package com.omer.clickgame;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by bimstajyer1 on 8.02.2018.
 */

@RealmClass
public class LocalHighScore extends RealmObject {

    private int score;

    public LocalHighScore() {
    }

    public LocalHighScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
