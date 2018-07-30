package motif.sample.app.photo_grid_item;

import motif.Scope;
import motif.sample.lib.controller.ViewlessControllerObjects;

@Scope
public interface PhotoGridItemScope {

    PhotoGridItemController controller();

    @motif.Objects
    abstract class Objects extends ViewlessControllerObjects<PhotoGridItemController> {}
}
