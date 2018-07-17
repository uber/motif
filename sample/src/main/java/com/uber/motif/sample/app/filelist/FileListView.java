package com.uber.motif.sample.app.filelist;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class FileListView extends RecyclerView {

    public FileListView(Context context) {
        this(context, null, 0);
    }

    public FileListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FileListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutManager(new LinearLayoutManager(context));
    }
}
