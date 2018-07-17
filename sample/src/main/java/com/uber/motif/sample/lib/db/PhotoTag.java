package com.uber.motif.sample.lib.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

@Entity(
        tableName = "photo_tag",
        primaryKeys = {"photo_id", "tag"},
        indices = {@Index("photo_id"), @Index("tag")},
        foreignKeys = {
                @ForeignKey(entity = Photo.class,
                        parentColumns = "id",
                        childColumns = "photo_id"),
                @ForeignKey(entity = Tag.class,
                        parentColumns = "name",
                        childColumns = "tag")
        }
)
public class PhotoTag {

    @ColumnInfo(name = "photo_id")
    public final int photoId;

    @NonNull
    public final String tag;

    public PhotoTag(int photoId, @NonNull String tag) {
        this.photoId = photoId;
        this.tag = tag;
    }
}
