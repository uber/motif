package com.uber.motif.sample.app.root;

import android.content.Context;
import android.view.ViewGroup;
import com.uber.motif.sample.R;
import com.uber.motif.sample.app.photolist.PhotoListView;
import com.uber.motif.sample.app.photorow.PhotoClickListener;
import com.uber.motif.sample.app.photorow.PhotoLongClickListener;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.lib.db.Database;
import com.uber.motif.sample.lib.db.Photo;

class RootController extends Controller<ViewGroup> implements PhotoClickListener, PhotoLongClickListener {

    private final RootScope scope;
    private final Context context;
    private Database database;

    RootController(RootScope scope, Context context, Database database) {
        super(context, null, R.layout.root);
        this.scope = scope;
        this.context = context;
        this.database = database;
    }

    @Override
    public void onClick(Photo photo) {
        System.out.println("Click: " + photo);
    }

    @Override
    public void onLongClick(Photo photo) {
        System.out.println("Long Click: " + photo);
    }

    @Override
    protected void onAttach() {
        database.populateIfNecessary()
                .as(autoDispose())
                .subscribe(() -> {
                    PhotoListView photoListView = scope.photoList(getView()).view();
                    getView().addView(photoListView);
                });
    }
}
