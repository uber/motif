package com.uber.motif.sample.app.photolist;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class PhotoListView extends RecyclerView {

    public PhotoListView(Context context) {
        this(context, null, 0);
    }

    public PhotoListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutManager(new LinearLayoutManager(context));
    }
}
