package net.thangvnnc.appmap.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.util.Date;

import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_ID;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE;
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
    public long createdBy;
    public long updatedBy;

    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE).orderByChild(FB_DB_ID);
//            return mDatabase.child(FB_DB_NOTE).orderByChild(FB_DB_ID).limitToFirst(2).startAt("1606482538438", FB_DB_ID);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE).child(id).removeValue();
    }

    public FBLocation() {
    }

    public Task<Void> insertOrUpdateUser() {
        return mDatabase.child(FB_DB_NOTE).child(this.id).setValue(this);
    }

    public Task<Void> remove() {
        return mDatabase.child(FB_DB_NOTE).child(this.id).removeValue();
    }
}