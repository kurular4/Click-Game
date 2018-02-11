package com.omer.clickgame;

/**
 * Created by bimstajyer1 on 8.02.2018.
 */

public class Level {

    int necessaryClick;
    int time;
    int level;


    public Level(int necessaryClick, int time, int level) {
        this.necessaryClick = necessaryClick;
        this.time = time;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getNecessaryClick() {
        return necessaryClick;
    }

    public void setNecessaryClick(int necessaryClick) {
        this.necessaryClick = necessaryClick;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
