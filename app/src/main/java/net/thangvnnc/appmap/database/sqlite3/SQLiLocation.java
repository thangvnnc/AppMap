package net.thangvnnc.appmap.database.sqlite3;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import net.thangvnnc.appmap.common.ContextUtils;
import net.thangvnnc.appmap.database.Location;

import static net.thangvnnc.appmap.database.sqlite3.SQLiteDB.DATABASE_NAME;
import static net.thangvnnc.appmap.database.sqlite3.SQLiteDB.DATABASE_VERSION;

@Database(entities = {Location.class}, version = DATABASE_VERSION, exportSchema = false)
public abstract class SQLiLocation extends RoomDatabase {
    private static SQLiLocation sqliLocation;

    public abstract SQLiLocationDao sqliLocationDao();

    private static SQLiLocation getInstance(Context context) {
        if (sqliLocation == null) {
            sqliLocation = Room.databaseBuilder(context, SQLiLocation.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return sqliLocation;
    }

    public static SQLiLocationDao getDao() {
        return SQLiLocation.getInstance(ContextUtils.getContext()).sqliLocationDao();
    }
}