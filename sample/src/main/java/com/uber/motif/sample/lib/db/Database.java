package com.uber.motif.sample.lib.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.RequiresPermission;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.util.List;

public class Database {

    private static final String SHARED_PREFS_FILENAME = "photo-cache";
    private static final String SHARED_PREFS_POPULATED_KEY = "photo-cache-populated";

    private final SharedPreferences sharedPreferences;
    private final FileSystem fileSystem;
    private final PhotoDao photoDao;

    public Database(Context context) {
        this(context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE),
                new FileSystem(context),
                RoomDatabase.create(context));
    }

    Database(
            SharedPreferences sharedPreferences,
            FileSystem fileSystem,
            RoomDatabase database) {
        this.sharedPreferences = sharedPreferences;
        this.fileSystem = fileSystem;
        this.photoDao = database.photoDao();
    }

    public Single<List<Photo>> allPhotos() {
        return photoDao.getPhotos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Tag>> tagsForPhoto(int photoId) {
        return photoDao.getTagsForPhoto(photoId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Photo>> photosForTag(String tag) {
        return photoDao.getPhotosForTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable tagPhoto(int photoId, String tag) {
        return Completable.fromAction(() -> {
            photoDao.insert(new Tag(tag));
            photoDao.insert(new PhotoTag(photoId, tag));
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public boolean isPopulated() {
        return sharedPreferences.getBoolean(SHARED_PREFS_POPULATED_KEY, false);
    }

    @RequiresPermission("android.permission.READ_EXTERNAL_STORAGE")
    public Completable populateIfNecessary() {
        if (isPopulated()) {
            return Completable.complete();
        } else {
            return populate();
        }
    }

    @RequiresPermission("android.permission.READ_EXTERNAL_STORAGE")
    public Completable populate() {
        return Completable.fromAction(() -> {
            Iterable<Photo> photos = fileSystem.allPhotos();
            photoDao.insert(photos);
            sharedPreferences.edit().putBoolean(SHARED_PREFS_POPULATED_KEY, true).apply();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
