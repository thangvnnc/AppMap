package net.thangvnnc.appmap.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "locations")
public class Location extends ModelBase {
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "lat")
    public float lat;

    @ColumnInfo(name = "lng")
    public float lng;

    public static List<Location> parseLocations(boolean isUsing, DataSnapshot snapshot) {
        List<Location> locationGets = new ArrayList<>();
        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
            Location location = postSnapshot.getValue(Location.class);
            if (isUsing == location.isUsing) {
                locationGets.add(location);
            }
        }

        return locationGets;
    }

    public Location() {
    }
}