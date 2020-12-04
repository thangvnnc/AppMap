package net.thangvnnc.appmap.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE_DIRECTION;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_PRIMARY_KEY_ID;
import static net.thangvnnc.appmap.database.FirebaseDB.mDatabase;

public class FBDirection implements Serializable {
    public String id;
    public int step;
    public List<String> locations;
    public List<String> locationDetails;
    public boolean isUsing;
    public Date createdAt;
    public Date updatedAt;
    public long createdBy;
    public long updatedBy;

    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).orderByChild(FB_DB_PRIMARY_KEY_ID);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(id).removeValue();
    }

    public FBDirection() {
    }

    public Task<Void> insertOrUpdateUser() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(this.id).setValue(this);
    }

    public Task<Void> remove() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(this.id).removeValue();
    }
}