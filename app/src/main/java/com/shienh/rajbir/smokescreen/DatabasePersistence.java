package com.shienh.rajbir.smokescreen;

import com.google.firebase.database.FirebaseDatabase;

public class DatabasePersistence {
    private static FirebaseDatabase mData;

    public static FirebaseDatabase getDatabase() {
        if (mData == null) {

            mData = FirebaseDatabase.getInstance();
            mData.setPersistenceEnabled(true);
        }
        return mData;
    }
}
