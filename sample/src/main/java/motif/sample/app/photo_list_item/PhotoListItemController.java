package motif.sample.app.photo_list_item;

import motif.sample.lib.controller.Controller;
import motif.sample.lib.db.Photo;

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
