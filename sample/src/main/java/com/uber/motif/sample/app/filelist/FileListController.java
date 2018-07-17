package com.uber.motif.sample.app.filelist;

import android.content.Context;
import android.view.ViewGroup;
import com.uber.controller.Controller;
import com.uber.motif.sample.R;
import com.uber.motif.sample.model.RootDir;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileListController extends Controller<FileListView> {

    private final FileListAdapter adapter;
    private final RootDir rootDir;

    public FileListController(
            Context context,
            ViewGroup parent,
            FileListAdapter adapter,
            RootDir rootDir) {
        super(context, parent, R.layout.filelist);
        this.adapter = adapter;
        this.rootDir = rootDir;
    }

    @Override
    protected void onAttach() {
        List<File> files = Arrays.asList(rootDir.getFile().listFiles());
        adapter.setFiles(files);
        getView().setAdapter(adapter);
    }
}
