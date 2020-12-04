package net.thangvnnc.appmap.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDB {
    public static final String FB_DB_NOTE_LOCATION = "Locations";
    public static final String FB_DB_NOTE_DIRECTION = "Directions";
    public static final String FB_DB_PRIMARY_KEY_ID = "id";
    public static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public static String generalLocationId() {
        return System.currentTimeMillis() + "";
    }
}

