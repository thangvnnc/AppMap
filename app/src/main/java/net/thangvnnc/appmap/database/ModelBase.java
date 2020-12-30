package net.thangvnnc.appmap.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ModelBase implements Serializable {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "isUsing")
    public boolean isUsing;

    @ColumnInfo(name = "createdAt")
    public String createdAt;

    @ColumnInfo(name = "updatedAt")
    public String updatedAt;

    @ColumnInfo(name = "createdBy")
    public String createdBy;

    @ColumnInfo(name = "updatedBy")
    public String updatedBy;

    public static String generalId() {
        return UUID.randomUUID().toString();
    }
}
