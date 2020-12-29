package net.thangvnnc.appmap.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE_LOCATION;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_PRIMARY_KEY_ID;
import static net.thangvnnc.appmap.database.FirebaseDB.mDatabase;

public class FBLocation implements Serializable {
    public String id;
    public String name;
    public String description;
    public float lat;
    public float lng;
    public boolean isUsing;
    public Date createdAt;
    public Date updatedAt;
    public String createdBy;
    public String updatedBy;

    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_LOCATION).orderByChild("createdBy").equalTo(FBUser.getSession().id);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_LOCATION);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(id).removeValue();
    }

    public static List<FBLocation> parseLocations(boolean isUsing, DataSnapshot snapshot) {
        List<FBLocation> fbLocationGets = new ArrayList<>();
        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
            FBLocation fbLocation = postSnapshot.getValue(FBLocation.class);
            if (isUsing == fbLocation.isUsing) {
                fbLocationGets.add(fbLocation);
            }
        }

        return fbLocationGets;
    }

    public FBLocation() {
    }

    public Task<Void> insertOrUpdate() {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(this.id).setValue(this);
    }

    public Task<Void> remove() {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(this.id).removeValue();
    }
}