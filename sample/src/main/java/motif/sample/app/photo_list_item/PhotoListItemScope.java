package motif.sample.app.photo_list_item;

import motif.Scope;
import motif.sample.lib.controller.ViewlessControllerObjects;

@Scope
public interface PhotoListItemScope {

    PhotoListItemController controller();

    abstract class Objects extends ViewlessControllerObjects<PhotoListItemController> {}
}
