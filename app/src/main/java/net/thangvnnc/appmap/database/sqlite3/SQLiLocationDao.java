package net.thangvnnc.appmap.database.sqlite3;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import net.thangvnnc.appmap.database.Location;

import java.util.List;

@Dao
public abstract class SQLiLocationDao {

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Insert
    abstract void insert(Location location);

    @Insert
    public void inserts(List<Location> locations) {
        for (Location location: locations) {
            insert(location);
        }
    }

    @Query("SELECT * FROM locations WHERE id = :id")
    abstract List<Location> getOwner(int id);


    @Query("SELECT * FROM locations")
    public abstract List<Location> getAll();
}