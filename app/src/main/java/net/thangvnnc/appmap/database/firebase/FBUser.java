package net.thangvnnc.appmap.database.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import net.thangvnnc.appmap.database.User;

import static net.thangvnnc.appmap.database.firebase.FirebaseDB.FB_DB_NOTE_USER;
import static net.thangvnnc.appmap.database.firebase.FirebaseDB.FB_DB_PRIMARY_KEY_ID;
import static net.thangvnnc.appmap.database.firebase.FirebaseDB.mDatabase;

public class FBUser {

    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_USER).orderByChild(FB_DB_PRIMARY_KEY_ID);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_USER);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_USER).child(id).removeValue();
    }

    public static Task<Void> insertOrUpdate(User user) {
        return mDatabase.child(FB_DB_NOTE_USER).child(user.id).setValue(user);
    }
}
