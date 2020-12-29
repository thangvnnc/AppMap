package net.thangvnnc.appmap.database;

import com.directions.route.Route;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE_DIRECTION;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_PRIMARY_KEY_ID;
import static net.thangvnnc.appmap.database.FirebaseDB.mDatabase;

public class FBDirection implements Serializable {
    public static final String TAG = FBDirection.class.getName();
    public String id;
    public int step;
    public List<String> locations;
    public List<Route> locationDetails;
    public int orderByNum;
    public boolean isUsing;
    public Date createdAt;
    public Date updatedAt;
    public String createdBy;
    public String updatedBy;

    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).orderByChild("createdBy").equalTo(FBUser.getSession().id);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(id).removeValue();
    }

    public static List<FBDirection> parseDirections(boolean isUsing, DataSnapshot snapshot) {
        List<FBDirection> fBDirectionGets = new ArrayList<>();
        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
            FBDirection FBDirection = postSnapshot.getValue(FBDirection.class);
            if (isUsing == FBDirection.isUsing) {
                fBDirectionGets.add(FBDirection);
            }
        }

        Collections.sort(fBDirectionGets, (o1, o2) -> o2.orderByNum - o1.orderByNum);

        return fBDirectionGets;
    }

    public FBDirection() {
    }

    public Task<Void> insertOrUpdate() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(this.id).setValue(this);
    }

    public Task<Void> remove() {
        return mDatabase.child(FB_DB_NOTE_DIRECTION).child(this.id).removeValue();
    }
}