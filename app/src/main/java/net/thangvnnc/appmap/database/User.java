package net.thangvnnc.appmap.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.thangvnnc.appmap.common.ContextUtils;
import net.thangvnnc.appmap.common.DateUtils;
import net.thangvnnc.appmap.common.SharedPreferencesManager;
import net.thangvnnc.appmap.database.firebase.FBUser;

import java.util.Date;

@Entity(tableName = "users")
public class User extends ModelBase {
    public interface LoginResult {
        void success (User user);
    }

    public static final String TAG = User.class.getName();

    @ColumnInfo(name = "typeAccount")
    public String typeAccount;

    @ColumnInfo(name = "idAcc")
    public String idAcc;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "imgAvatar")
    public String imgAvatar;

    @ColumnInfo(name = "lastLogin")
    public String lastLogin;

    public static void login(String idAcc, String typeAccount, String name, String imgAvatar, LoginResult loginResult) {
        Query query = FBUser.getChild().orderByChild("idAcc").equalTo(idAcc);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = null;
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    user = postSnapshot.getValue(User.class);
                    if (user.typeAccount.equals(typeAccount)) {
                        break;
                    }
                }

                // Set session
                User.saveSession(user);

                if (user == null) {
                    user = new User();
                    user.id = User.generalId();
                    user.createdAt = DateUtils.getCurrent();
                    user.createdBy = user.id;
                }

                user.idAcc = idAcc;
                user.typeAccount = typeAccount;
                user.name = name;
                user.imgAvatar = imgAvatar;
                user.isUsing = true;
                user.lastLogin = DateUtils.getCurrent();
                user.updatedAt = DateUtils.getCurrent();
                user.updatedBy = user.id;

                FBUser.insertOrUpdate(user);

                if (loginResult == null) return;
                loginResult.success(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    public static void saveSession(User user) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String sessionString = gson.toJson(user);
        Context context = ContextUtils.getContext();
        SharedPreferencesManager.setString(context, SharedPreferencesManager.SESSION_USER_LOGIN, sessionString);
    }

    public static User getSession() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        Context context = ContextUtils.getContext();
        String sessionString = SharedPreferencesManager.getString(context, SharedPreferencesManager.SESSION_USER_LOGIN);
        return gson.fromJson(sessionString, User.class);
    }

    public static void logout(Context context) {
        User session = getSession();
        if (session == null) {
            throw new UnsupportedOperationException("Null session User");
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
    }

    public User() {
    }

    public final class TYPE {
        public static final String GOOGLE = "GOOGLE";
        public static final String FACEBOOK = "FACEBOOK";
    }
}