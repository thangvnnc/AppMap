package net.thangvnnc.appmap.database.sqlite3;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import net.thangvnnc.appmap.database.User;

@Dao
public interface SQLiUser {

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Insert
    void insert(User user);

    @Insert
    void inserts(User... users);
}