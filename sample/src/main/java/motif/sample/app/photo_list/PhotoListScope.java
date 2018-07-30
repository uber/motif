package motif.sample.app.photo_list;

import motif.Scope;
import motif.sample.app.photo_list_item.PhotoListItemScope;
import motif.sample.app.photo_list_item.PhotoListItemView;
import motif.sample.lib.controller.ControllerObjects;
import motif.sample.lib.db.Photo;

@Scope
public interface PhotoListScope {

    PhotoListView view();

    PhotoListItemScope item(PhotoListItemView view, Photo photo);

    @motif.Objects
    abstract class Objects extends ControllerObjects<PhotoListController, PhotoListView> {

        abstract PhotoListAdapter adapter();
    }
}
