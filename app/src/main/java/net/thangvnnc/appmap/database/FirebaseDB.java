package net.thangvnnc.appmap.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.Date;

public class FirebaseDB {
    private static final String FB_DB_NOTE = "Locations";
    private static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public static class FBLocation implements Serializable {
        public String id;
        public String name;
        public String description;
        public float lat;
        public float lng;
        public boolean isUsing;
        public Date createdAt;
        public Date updatedAt;
        public long createdBy;
        public long updatedBy;

        public static DatabaseReference getAll() {
            return mDatabase.child(FB_DB_NOTE);
        }

        public FBLocation() {
        }

        public Task<Void> insertOrUpdateUser() {
            return mDatabase.child(FB_DB_NOTE).child(this.id).setValue(this);
        }
    }
}

