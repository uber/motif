package com.uber.motif.sample.app.photorow;

import com.uber.motif.sample.lib.db.Photo;
import io.reactivex.Observable;

public interface PhotoTouches {

    Observable<Photo> touches();
}
