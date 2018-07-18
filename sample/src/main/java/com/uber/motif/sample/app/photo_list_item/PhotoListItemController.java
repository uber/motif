package com.uber.motif.sample.app.photo_list_item;

import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.lib.db.Photo;

public class PhotoListItemController extends Controller<PhotoListItemView> {

    private final Photo photo;

    public PhotoListItemController(PhotoListItemView view, Photo photo) {
        super(view, false);
        this.photo = photo;
    }

    @Override
    protected void onAttach() {
        view.setPhoto(photo);
    }
}
