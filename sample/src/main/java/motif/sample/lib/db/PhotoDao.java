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