package net.thangvnnc.appmap.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE_DIRECTION;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_PRIMARY_KEY_ID;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE_LOCATION;
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
        return mDatabase.child(FB_DB_NOTE_LOCATION).orderByChild(FB_DB_PRIMARY_KEY_ID);
//            return mDatabase.child(FB_DB_NOTE).orderByChild(FB_DB_ID).limitToFirst(2).startAt("1606482538438", FB_DB_ID);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_LOCATION);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(id).removeValue();
    }

    public FBLocation() {
    }

    public Task<Void> insertOrUpdateUser() {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(this.id).setValue(this);
    }

    public Task<Void> remove() {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(this.id).removeValue();
    }
}