package net.thangvnnc.appmap.database.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import net.thangvnnc.appmap.database.Direction;
import net.thangvnnc.appmap.database.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.thangvnnc.appmap.database.firebase.FirebaseDB.FB_DB_NOTE_DIRECTION;
import static net.thangvnnc.appmap.database.firebase.FirebaseDB.mDatabase;

public class FBDirection {
    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).orderByChild("createdBy").equalTo(User.getSession().id);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(id).removeValue();
    }

    public static List<Direction> parseDirections(boolean isUsing, DataSnapshot snapshot) {
        List<Direction> fBDirections = new ArrayList<>();
        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
            Direction Direction = postSnapshot.getValue(Direction.class);
            if (isUsing == Direction.isUsing) {
                fBDirections.add(Direction);
            }
        }

        Collections.sort(fBDirections, (o1, o2) -> o2.orderByNum - o1.orderByNum);

        return fBDirections;
    }

    public static Task<Void> insertOrUpdate(Direction direction) {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(direction.id).setValue(direction);
    }
}
