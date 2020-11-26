package net.thangvnnc.appmap;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import net.thangvnnc.appmap.database.FirebaseDB;
import net.thangvnnc.appmap.service.GPSService;

import java.util.Date;

public class MainBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_base);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_stores, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        if (!GPSService.isRunning(this, GPSService.class)) {
            GPSService.startService(this);
        }

//        FirebaseDB.FBLocation fbLocation = new FirebaseDB.FBLocation();
//        fbLocation.id = "1";
//        fbLocation.title = "title123";
//        fbLocation.description = "description";
//        fbLocation.createdAt = new Date();
//        fbLocation.lat = 10.6f;
//        fbLocation.lng = 106f;
//        fbLocation.insertOrUpdateUser();
    }

}