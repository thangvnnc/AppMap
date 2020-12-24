package net.thangvnnc.appmap.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int LOGIN_ACTIVITY_RC_SIGN_IN = 1000;

    public ActivityLoginBinding mBind = null;
    private Context mContext = null;
    private GoogleSignInClient mGoogleSignInClient = null;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null){
            supportActionBar.hide();
        }
        mContext = this;
        mBind = ActivityLoginBinding.inflate(LayoutInflater.from(mContext));
        setContentView(mBind.getRoot());

        checkLoginExist();

        mBind.btnSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mBind.btnSignInButton.setOnClickListener(btnSignInButtonClick);
    }

    private void checkLoginExist() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
        if (account != null) {
            startMainActivity();
        }
    }

    private final View.OnClickListener btnSignInButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mProgressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, LOGIN_ACTIVITY_RC_SIGN_IN);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == LOGIN_ACTIVITY_RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            startMainActivity();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(mContext, "signInResult:failed code=" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        // Signed in successfully, show authenticated UI.
        Intent intentMain = new Intent(mContext, MainBaseActivity.class);
        startActivity(intentMain);
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}