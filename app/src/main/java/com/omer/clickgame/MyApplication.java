package com.omer.clickgame;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by bimstajyer1 on 8.02.2018.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("localscores.realm").build();
        Realm.setDefaultConfiguration(config);
    }
}

