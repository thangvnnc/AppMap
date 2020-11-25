package net.thangvnnc.appmap.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class FirebaseDB {
    private static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public static class FBLocation {
        public String id;
        public String title;
        public String description;
        public float lat;
        public float lng;
        public Date createdAt;

        public FBLocation() {
        }

        public void writeNewUser() {
            mDatabase.child("Locations").child(this.id).setValue(this);
        }

        public void getAll() {
        }
    }
}

