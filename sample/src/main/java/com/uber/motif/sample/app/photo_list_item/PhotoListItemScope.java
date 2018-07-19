package com.uber.motif.sample.app.photo_list_item;

import com.uber.motif.Scope;
import com.uber.motif.sample.lib.controller.ViewlessControllerObjects;

@Scope
public interface PhotoListItemScope {

    PhotoListItemController controller();

    abstract class Objects extends ViewlessControllerObjects<PhotoListItemController> {}
}
