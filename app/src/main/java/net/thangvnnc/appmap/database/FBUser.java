package net.thangvnnc.appmap.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.util.Date;

import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE_USER;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_PRIMARY_KEY_ID;
import static net.thangvnnc.appmap.database.FirebaseDB.mDatabase;

public class FBUser implements Serializable {
    public String id;
    public int typeAccount;
    public String idAcc;
    public String name;
    public boolean isUsing;
    public Date createdAt;
    public Date updatedAt;
    public long createdBy;
    public long updatedBy;

    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_USER).orderByChild(FB_DB_PRIMARY_KEY_ID);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_USER);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_USER).child(id).removeValue();
    }

    public FBUser() {
    }

    public Task<Void> insertOrUpdateUser() {
        return mDatabase.child(FB_DB_NOTE_USER).child(this.id).setValue(this);
    }

    public Task<Void> remove() {
        return mDatabase.child(FB_DB_NOTE_USER).child(this.id).removeValue();
    }
}