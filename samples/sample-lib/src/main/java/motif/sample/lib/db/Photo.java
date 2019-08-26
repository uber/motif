/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.sample.lib.db;

import android.database.Cursor;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.File;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.DATE_TAKEN;
import static android.provider.MediaStore.Images.ImageColumns.LATITUDE;
import static android.provider.MediaStore.Images.ImageColumns.LONGITUDE;
import static android.provider.MediaStore.MediaColumns.*;

@Entity(
        tableName = "photo",
        indices = {@Index("timestamp")})
public class Photo {

    @PrimaryKey
    public final int id;
    public final String title;
    public final long timestamp;
    public final double latitude;
    public final double longitude;
    public final File location;
    @ColumnInfo(name = "mime_type")
    public final String mimeType;
    public final int size;

    public Photo(int id, String title, long timestamp, double latitude, double longitude, File location, String mimeType, int size) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.mimeType = mimeType;
        this.size = size;
    }

    public static Photo fromCursor(Cursor c) {
        int id = c.getInt(c.getColumnIndex(_ID));
        String title = c.getString(c.getColumnIndex(TITLE));
        long timestamp = c.getLong(c.getColumnIndex(DATE_TAKEN));
        double latitude = c.getInt(c.getColumnIndex(LATITUDE));
        double longitude = c.getInt(c.getColumnIndex(LONGITUDE));
        File location = new File(c.getString(c.getColumnIndex(DATA)));
        String mimeType = c.getString(c.getColumnIndex(MIME_TYPE));
        int size = c.getInt(c.getColumnIndex(SIZE));
        return new Photo(id, title, timestamp, latitude, longitude, location, mimeType, size);
    }
}
