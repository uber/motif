package com.uber.motif.sample.app.photo_grid;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class PhotoGridView extends RecyclerView {

    public PhotoGridView(Context context) {
        this(context, null);
    }

    public PhotoGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutManager(new GridLayoutManager(context, 2));
    }
}
