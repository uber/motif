package motif.sample.app.photo_list;


import android.view.ViewGroup;

import motif.sample.app.photo_list_item.PhotoListItemView;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.controller.ControllerAdapter;
import motif.sample.lib.db.Photo;
import motif.sample.lib.photo.PhotoDiffItemCallback;

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
