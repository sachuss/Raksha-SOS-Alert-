package com.vedantamadam.raksha;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;

//For maintaining global application state. This is created to initialise the realm database.

public class RakshaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name(getString(R.string.db_name))
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        Log.v("raksha", "Initializing Realm, file path: " + realmConfig.getPath());

    }
}
