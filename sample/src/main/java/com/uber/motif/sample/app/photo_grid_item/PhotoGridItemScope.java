package com.uber.motif.sample.app.photo_grid_item;

import com.uber.motif.Scope;

@Scope
public interface PhotoGridItemScope {

    PhotoGridItemController controller();

    abstract class Objects {

        abstract PhotoGridItemController controller();
    }
}
