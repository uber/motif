package com.uber.motif.sample.app.photolist;

import android.content.Context;
import android.view.ViewGroup;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.R;
import com.uber.motif.sample.lib.db.Database;

public class PhotoListController extends Controller<PhotoListView> {

    private final Database database;
    private final PhotoAdapter adapter;

    public PhotoListController(
            Context context,
            ViewGroup parent,
            Database database,
            PhotoAdapter adapter) {
        super(context, parent, R.layout.photolist);
        this.database = database;
        this.adapter = adapter;
    }

    @Override
    protected void onAttach() {
        database.allPhotos().as(autoDispose())
                .subscribe(photos -> {
                    adapter.setPhotos(photos);
                    getView().setAdapter(adapter);
                });
    }
}
