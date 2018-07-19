package com.uber.motif.sample.app.photo_grid_item;

import com.uber.motif.Scope;
import com.uber.motif.sample.lib.controller.ViewlessControllerObjects;

@Scope
public interface PhotoGridItemScope {

    PhotoGridItemController controller();

    abstract class Objects extends ViewlessControllerObjects<PhotoGridItemController> {}
}
