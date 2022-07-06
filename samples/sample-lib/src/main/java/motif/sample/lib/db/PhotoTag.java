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

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "photo_tag",
    primaryKeys = {"photo_id", "tag"},
    indices = {@Index("photo_id"), @Index("tag")},
    foreignKeys = {
      @ForeignKey(entity = Photo.class, parentColumns = "id", childColumns = "photo_id"),
      @ForeignKey(entity = Tag.class, parentColumns = "name", childColumns = "tag")
    })
public class PhotoTag {

  @ColumnInfo(name = "photo_id")
  public final int photoId;

  @NonNull public final String tag;

  public PhotoTag(int photoId, @NonNull String tag) {
    this.photoId = photoId;
    this.tag = tag;
  }
}
