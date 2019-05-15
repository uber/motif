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

import android.arch.persistence.room.*;
import io.reactivex.Single;

import java.util.List;

@Dao
public interface PhotoDao {
    @Query("SELECT * FROM photo")
    Single<List<Photo>> getPhotos();

    @Query("SELECT * FROM tag")
    Single<List<Tag>> getTags();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Iterable<Photo> users);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Tag tag);

    @Delete
    void delete(Photo photo);

    @Delete
    void delete(Tag tag);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PhotoTag photoTag);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM photo INNER JOIN photo_tag ON photo.id=photo_tag.photo_id WHERE photo_tag.tag=:tag")
    Single<List<Photo>> getPhotosForTag(String tag);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM tag INNER JOIN photo_tag ON tag.name=photo_tag.tag WHERE photo_tag.photo_id=:photoId")
    Single<List<Tag>> getTagsForPhoto(int photoId);
}
