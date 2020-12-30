package net.thangvnnc.appmap.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.database.User;
import net.thangvnnc.appmap.databinding.ActivityMainBaseBinding;
import net.thangvnnc.appmap.service.GPSService;
import net.thangvnnc.appmap.service.SyncDBService;

public class MainBaseActivity extends AppCompatActivity {
    private static final String TAG = "MainBaseActivity";
    public ActivityMainBaseBinding mBind = null;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mBind = ActivityMainBaseBinding.inflate(LayoutInflater.from(mContext));
        setContentView(mBind.getRoot());
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_stores, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mBind.navView, navController);
        runService();

//        List<Location> all = SQLiLocation.getDao().getAll();
//        Log.e("item location: ", String.valueOf(all.size()));
    }

    private void runService() {
        if (!GPSService.isRunning(this, GPSService.class)) {
            GPSService.startService(this);
        }

        if (!SyncDBService.isRunning(this, SyncDBService.class)) {
            SyncDBService.startService(this);
        }
    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        materialAlertDialogBuilder.setTitle(R.string.confirm_title_logout);
        materialAlertDialogBuilder.setMessage(R.string.confirm_content_logout);
        materialAlertDialogBuilder.setPositiveButton(R.string.confirm_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                User.logout(mContext);
                finish();
            }
        });
        materialAlertDialogBuilder.setNegativeButton(R.string.confirm_btn_no, null);
        materialAlertDialogBuilder.show();
    }
}