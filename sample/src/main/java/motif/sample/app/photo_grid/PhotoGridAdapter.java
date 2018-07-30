package motif.sample.app.photo_grid;


import android.view.ViewGroup;

import motif.sample.app.photo_grid_item.PhotoGridItemView;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.controller.ControllerAdapter;
import motif.sample.lib.db.Photo;
import motif.sample.lib.photo.PhotoDiffItemCallback;

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
