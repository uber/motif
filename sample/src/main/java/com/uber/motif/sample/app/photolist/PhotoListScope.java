package com.uber.motif.sample.app.photolist;

import com.uber.motif.Scope;
import com.uber.motif.sample.app.photorow.PhotoRowScope;
import com.uber.motif.sample.app.photorow.PhotoRowView;
import com.uber.motif.sample.app.photorow.PhotoTouches;
import com.uber.motif.sample.lib.db.Photo;

@Scope
public interface PhotoListScope {

    PhotoListView view();

    PhotoRowScope photoRow(PhotoRowView view, Photo photo);

    abstract class Objects {

        public abstract PhotoTouches photoTouches(PhotoAdapter adapter);

        abstract PhotoAdapter adapter();
        abstract PhotoListController controller();

        PhotoListView view(PhotoListController controller) {
            return controller.getView();
        }
    }
}
