package com.uber.motif.sample.app.photo_list_item;

import com.uber.motif.Scope;

@Scope
public interface PhotoListItemScope {

    PhotoListItemController controller();

    abstract class Objects {

        abstract PhotoListItemController controller();
    }
}
