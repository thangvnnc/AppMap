package net.thangvnnc.appmap.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Date;

import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_NOTE_USER;
import static net.thangvnnc.appmap.database.FirebaseDB.FB_DB_PRIMARY_KEY_ID;
import static net.thangvnnc.appmap.database.FirebaseDB.mDatabase;

public class FBUser implements Serializable {
    public static final String TAG = FBUser.class.getName();
    public String id;
    public String typeAccount;
    public String idAcc;
    public String name;
    public String imgAvatar;
    public boolean isUsing;
    public Date lastLogin;
    public Date createdAt;
    public Date updatedAt;
    public String createdBy;
    public String updatedBy;

    public static Query getAll() {
        return mDatabase.child(FB_DB_NOTE_USER).orderByChild(FB_DB_PRIMARY_KEY_ID);
    }

    public static DatabaseReference getChild() {
        return mDatabase.child(FB_DB_NOTE_USER);
    }

    public static Task<Void> removeById(String id) {
        return mDatabase.child(FB_DB_NOTE_USER).child(id).removeValue();
    }

    public static void login(String idAcc, String typeAccount, String name, String imgAvatar) {
        Query query = getChild().orderByChild("idAcc").equalTo(idAcc);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FBUser fbUser = null;
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    fbUser = postSnapshot.getValue(FBUser.class);
                    if (fbUser.typeAccount.equals(typeAccount)) {
                        break;
                    }
                }

                if (fbUser == null) {
                    fbUser = new FBUser();
                    fbUser.id = FirebaseDB.generalId();
                    fbUser.createdAt = new Date();
                    fbUser.createdBy = fbUser.id;
                }

                fbUser.idAcc = idAcc;
                fbUser.typeAccount = typeAccount;
                fbUser.name = name;
                fbUser.imgAvatar = imgAvatar;
                fbUser.isUsing = true;
                fbUser.lastLogin = new Date();
                fbUser.updatedAt = new Date();
                fbUser.updatedBy = fbUser.id;

                fbUser.insertOrUpdate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    public FBUser() {
    }

    public Task<Void> insertOrUpdate() {
        return mDatabase.child(FB_DB_NOTE_USER).child(this.id).setValue(this);
    }

    public Task<Void> remove() {
        return mDatabase.child(FB_DB_NOTE_USER).child(this.id).removeValue();
    }

    public final class TYPE {
        public static final String GOOGLE = "GOOGLE";
        public static final String FACEBOOK = "FACEBOOK";
    }
}