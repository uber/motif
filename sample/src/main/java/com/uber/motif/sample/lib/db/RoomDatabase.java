package com.uber.motif.sample.lib.db;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@android.arch.persistence.room.Database(
        entities = {Photo.class, Tag.class, PhotoTag.class},
        version = 1,
        exportSchema = false)
@TypeConverters({FileTypeConverter.class})
public abstract class RoomDatabase extends android.arch.persistence.room.RoomDatabase {
    public abstract PhotoDao photoDao();

    public static RoomDatabase create(Context context) {
        return Room.databaseBuilder(context, RoomDatabase.class, "motif-sample").build();
    }
}