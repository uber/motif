package motif.sample.app.photo_grid;

import motif.Scope;
import motif.sample.app.photo_grid_item.PhotoGridItemScope;
import motif.sample.app.photo_grid_item.PhotoGridItemView;
import motif.sample.lib.controller.ControllerObjects;
import motif.sample.lib.db.Photo;

@Scope
public interface PhotoGridScope {

    PhotoGridView view();

    PhotoGridItemScope photoRow(PhotoGridItemView view, Photo photo);

    @motif.Objects
    abstract class Objects extends ControllerObjects<PhotoGridController, PhotoGridView> {

        abstract PhotoGridAdapter adapter();
    }
}
