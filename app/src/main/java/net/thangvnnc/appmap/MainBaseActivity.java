package net.thangvnnc.appmap;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import net.thangvnnc.appmap.databinding.ActivityMainBaseBinding;
import net.thangvnnc.appmap.service.GPSService;

public class MainBaseActivity extends AppCompatActivity {
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
        if (!GPSService.isRunning(this, GPSService.class)) {
            GPSService.startService(this);
        }
    }

}