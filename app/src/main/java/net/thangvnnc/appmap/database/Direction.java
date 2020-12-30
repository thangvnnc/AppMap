package net.thangvnnc.appmap.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.directions.route.Route;

import java.util.List;

@Entity(tableName = "directions")
public class Direction extends ModelBase {
    public static final String TAG = Direction.class.getName();

    @ColumnInfo(name = "step")
    public int step;

    @ColumnInfo(name = "locations")
    public List<String> locations;

    @ColumnInfo(name = "locationDetails")
    public List<Route> locationDetails;

    @ColumnInfo(name = "orderByNum")
    public int orderByNum;

    public Direction() {
    }
}