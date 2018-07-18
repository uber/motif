package com.uber.motif.sample.app.photo_grid;


import android.view.ViewGroup;

import com.uber.motif.sample.app.photo_grid_item.PhotoGridItemView;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.lib.controller.ControllerAdapter;
import com.uber.motif.sample.lib.db.Photo;
import com.uber.motif.sample.lib.photo.PhotoDiffItemCallback;

class PhotoGridAdapter extends ControllerAdapter<Photo, PhotoGridItemView> {

    PhotoGridAdapter(PhotoGridScope scope) {
        super(new ControllerAdapter.Factory<Photo, PhotoGridItemView>() {
            @Override
            public Controller controller(PhotoGridItemView view, Photo item) {
                return scope.photoRow(view, item).controller();
            }

            @Override
            public PhotoGridItemView view(ViewGroup parent) {
                return PhotoGridItemView.create(parent);
            }
        }, new PhotoDiffItemCallback());
    }
}
