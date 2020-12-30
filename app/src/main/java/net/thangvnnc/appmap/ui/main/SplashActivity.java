package net.thangvnnc.appmap.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.common.DateUtils;
import net.thangvnnc.appmap.database.User;
import net.thangvnnc.appmap.database.firebase.FBDirection;
import net.thangvnnc.appmap.database.firebase.FBLocation;
import net.thangvnnc.appmap.databinding.ActivitySplashBinding;

import java.util.Date;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    public ActivitySplashBinding mBind = null;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null){
            supportActionBar.hide();
        }
        mContext = this;
        mBind = ActivitySplashBinding.inflate(LayoutInflater.from(mContext));
        setContentView(mBind.getRoot());

        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));

//        User user = new User();
//        user.id = "1234567890";
//        user.name = "thang";
//        user.idAcc = "1234567890";
//        user.createdAt = DateUtils.getCurrent();
//        user.createdBy = user.id;
//        user.updatedAt = DateUtils.getCurrent();
//        user.updatedBy = user.id;
//        user.isUsing = true;
//        user.imgAvatar = "";
//        user.lastLogin = DateUtils.getCurrent();
//        user.typeAccount = User.TYPE.GOOGLE;
//        User.saveSession(user);

        User session = User.getSession();
        if (session != null) {
            startMainActivity();
        }
        else {
            startLoginActivity();
        }
        progressDialog.dismiss();

//        FBLocation.getChild().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
//                    appleSnapshot.getRef().removeValue();
//                }
//                Log.e(TAG, "remove all");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "onCancelled", databaseError.toException());
//            }
//        });
//        FBDirection.getChild().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
//                    appleSnapshot.getRef().removeValue();
//                }
//                Log.e(TAG, "remove all");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "onCancelled", databaseError.toException());
//            }
//        });
    }

    private void startLoginActivity() {
        Intent intentLogin = new Intent(mContext, LoginActivity.class);
        startActivity(intentLogin);
        finish();
    }

    private void startMainActivity() {
        Intent intentMain = new Intent(mContext, MainBaseActivity.class);
        startActivity(intentMain);
        finish();
    }
}