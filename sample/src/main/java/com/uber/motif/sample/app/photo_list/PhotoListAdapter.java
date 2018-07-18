package com.uber.motif.sample.app.photo_list;


import android.view.ViewGroup;

import com.uber.motif.sample.app.photo_list_item.PhotoListItemView;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.lib.controller.ControllerAdapter;
import com.uber.motif.sample.lib.db.Photo;
import com.uber.motif.sample.lib.photo.PhotoDiffItemCallback;

public class PhotoListAdapter extends ControllerAdapter<Photo, PhotoListItemView> {

    public PhotoListAdapter(PhotoListScope scope) {
        super(new Factory<Photo, PhotoListItemView>() {
            @Override
            public Controller controller(PhotoListItemView view, Photo item) {
                return scope.item(view, item).controller();
            }

            @Override
            public PhotoListItemView view(ViewGroup parent) {
                return PhotoListItemView.create(parent);
            }
        }, new PhotoDiffItemCallback());
    }
}
