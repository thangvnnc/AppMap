package net.thangvnnc.appmap.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
    public interface LoginResult {
        void success (FBUser fbUser);
    }

    private static FBUser session = null;
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

    public static void login(String idAcc, String typeAccount, String name, String imgAvatar, LoginResult loginResult) {
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

                // Set session
                session = fbUser;

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

                if (loginResult == null) return;
                loginResult.success(session);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    public static FBUser getSession() {
        return session;
    }

    public static void logout(Context context) {
        if (session == null) {
            throw new UnsupportedOperationException("Null session FBUser");
        }
        switch (session.typeAccount) {
            case TYPE.GOOGLE:
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
                googleSignInClient.signOut();
                break;

            case TYPE.FACEBOOK:
                throw new UnsupportedOperationException("Logout facebook!");

            default:
                throw new UnsupportedOperationException("Logout orther!");
        }

        session = null;
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