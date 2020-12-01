package net.thangvnnc.appmap.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDB {
    public static final String FB_DB_NOTE = "Locations";
    public static final String FB_DB_ID = "id";
    public static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
}

