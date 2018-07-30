package motif.sample.app.photo_grid_item;

import motif.sample.lib.multiselect.MultiSelector;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.db.Photo;

import butterknife.ButterKnife;

public class PhotoGridItemController extends Controller<PhotoGridItemView> {

    private final PhotoGridItemScope scope;
    private final MultiSelector multiSelector;
    private final Photo photo;

    PhotoGridItemController(
            PhotoGridItemScope scope,
            PhotoGridItemView view,
            MultiSelector multiSelector,
            Photo photo) {
        super(view, false);
        this.scope = scope;
        this.multiSelector = multiSelector;
        this.photo = photo;
        ButterKnife.bind(this, view);
    }

    @Override
    protected void onAttach() {
        view.setPhoto(photo);

        view.clicks()
                .as(autoDispose())
                .subscribe(o -> {
                    if (multiSelector.isSelected(photo)) {
                        multiSelector.deselect(photo);
                    } else {
                        multiSelector.select(photo);
                    }
                });

        multiSelector.selected()
                .as(autoDispose())
                .subscribe(photos -> {
                    view.setSelected(multiSelector.isSelected(photo));
                });
    }
}
