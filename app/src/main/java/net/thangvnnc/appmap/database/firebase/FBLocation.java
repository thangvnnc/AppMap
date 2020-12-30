package net.thangvnnc.appmap.database.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import net.thangvnnc.appmap.database.Location;
import net.thangvnnc.appmap.database.User;

import static net.thangvnnc.appmap.database.firebase.FirebaseDB.FB_DB_NOTE_LOCATION;
import static net.thangvnnc.appmap.database.firebase.FirebaseDB.mDatabase;

public class FBLocation {
    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_LOCATION).orderByChild("createdBy").equalTo(User.getSession().id);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_LOCATION);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(id).removeValue();
    }

    public static Task<Void> insertOrUpdate(Location location) {
        return mDatabase.child(FB_DB_NOTE_LOCATION).child(location.id).setValue(location);
    }
}
