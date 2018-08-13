/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import java.util.Iterator;

public class FileSystem {

    private final Context context;

    public FileSystem(Context context) {
        this.context = context;
    }

    @RequiresPermission("android.permission.READ_EXTERNAL_STORAGE")
    public Iterable<Photo> allPhotos() {
        ContentResolver contentResolver = context.getContentResolver();
        String[] columns = new String[] {
                ImageColumns._ID,
                ImageColumns.TITLE,
                ImageColumns.DATE_TAKEN,
                ImageColumns.LATITUDE,
                ImageColumns.LONGITUDE,
                ImageColumns.DATA,
                ImageColumns.MIME_TYPE,
                ImageColumns.SIZE };
        Cursor c = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, null);
        return new PhotoCursor(c);
    }

    private static class PhotoCursor implements Iterable<Photo> {

        private final Cursor c;

        PhotoCursor(Cursor c) {
            this.c = c;
        }

        @NonNull
        @Override
        public Iterator<Photo> iterator() {
            return new Iterator<Photo>() {
                @Override
                public boolean hasNext() {
                    if (c.isClosed()) {
                        return false;
                    }
                    if (c.getCount() == 0) {
                        c.close();
                        return false;
                    }
                    return true;
                }

                @Override
                public Photo next() {
                    c.moveToNext();
                    Photo photo = Photo.fromCursor(c);
                    if (c.isLast() && !c.isClosed()) {
                        c.close();
                    }
                    return photo;
                }
            };
        }
    }
}
