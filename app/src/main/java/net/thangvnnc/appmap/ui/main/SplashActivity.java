package net.thangvnnc.appmap.ui.main;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import net.thangvnnc.appmap.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    public ActivitySplashBinding mBind = null;
    private Context mContext = null;
    private GoogleSignInClient mGoogleSignInClient = null;

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

        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, "Please waiting...");

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
        if (account != null) {
            startMainActivity();
        }
        else {
            startLoginActivity();
        }
        progressDialog.dismiss();
    }

    private void startLoginActivity() {
        Intent intentLogin = new Intent(mContext, LoginActivity.class);
        startActivity(intentLogin);
    }

    private void startMainActivity() {
        Intent intentMain = new Intent(mContext, MainBaseActivity.class);
        startActivity(intentMain);
    }
}