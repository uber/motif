package com.uber.motif.sample.app.root;

import android.content.Context;
import android.view.ViewGroup;
import com.uber.controller.Controller;
import com.uber.motif.sample.R;
import com.uber.motif.sample.app.filelist.FileListView;
import com.uber.motif.sample.app.filerow.FileClickListener;
import com.uber.motif.sample.app.filerow.FileLongClickListener;
import com.uber.motif.sample.model.RootDir;

import java.io.File;

class RootController extends Controller<ViewGroup> implements FileClickListener, FileLongClickListener {

    private final RootScope scope;
    private final Context context;
    private final RootDir rootDir;

    RootController(RootScope scope, Context context, RootDir rootDir) {
        super(context, null, R.layout.root);
        this.scope = scope;
        this.context = context;
        this.rootDir = rootDir;
    }

    @Override
    public void onClick(File file) {
        System.out.println("Click: " + file);
    }

    @Override
    public void onLongClick(File file) {
        System.out.println("Long Click: " + file);
    }

    @Override
    protected void onAttach() {
        FileListView fileListView = scope.fileList(getView()).view();
        getView().addView(fileListView);
    }
}
