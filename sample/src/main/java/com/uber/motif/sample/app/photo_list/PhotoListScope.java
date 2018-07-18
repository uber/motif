package com.uber.motif.sample.app.photo_list;

import com.uber.motif.Scope;
import com.uber.motif.sample.app.photo_list_item.PhotoListItemScope;
import com.uber.motif.sample.app.photo_list_item.PhotoListItemView;
import com.uber.motif.sample.lib.db.Photo;

@Scope
public interface PhotoListScope {

    PhotoListView view();

    PhotoListItemScope item(PhotoListItemView view, Photo photo);

    abstract class Objects {

        abstract PhotoListController controller();
        abstract PhotoListAdapter adapter();

        PhotoListView view(PhotoListController controller) {
            return controller.getView();
        }
    }
}
