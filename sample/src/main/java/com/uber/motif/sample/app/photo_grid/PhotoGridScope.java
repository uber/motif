package com.uber.motif.sample.app.photo_grid;

import com.uber.motif.Scope;
import com.uber.motif.sample.app.photo_grid_item.PhotoGridItemScope;
import com.uber.motif.sample.app.photo_grid_item.PhotoGridItemView;
import com.uber.motif.sample.lib.controller.ControllerObjects;
import com.uber.motif.sample.lib.db.Photo;

@Scope
public interface PhotoGridScope {

    PhotoGridView view();

    PhotoGridItemScope photoRow(PhotoGridItemView view, Photo photo);

    abstract class Objects extends ControllerObjects<PhotoGridController, PhotoGridView> {

        abstract PhotoGridAdapter adapter();
    }
}
