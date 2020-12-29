package net.thangvnnc.appmap.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.database.FBUser;
import net.thangvnnc.appmap.databinding.ActivitySplashBinding;

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

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
        if (account != null) {
            String imgAvatar = null;
            Uri photoUri = account.getPhotoUrl();
            if (photoUri != null) {
                imgAvatar = photoUri.getPath();
            }
            FBUser.login(account.getId(), FBUser.TYPE.GOOGLE, account.getDisplayName(), imgAvatar, fbUser -> startMainActivity());
        }
        else {
            startLoginActivity();
        }
        progressDialog.dismiss();
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