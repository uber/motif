package com.uber.motif.sample.filerow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.uber.motif.sample.R;

public class FileRowView extends FrameLayout {

    @Nullable
    private View overlayView;

    public FileRowView(Context context) {
        super(context);
    }

    public FileRowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FileRowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOverlay(View overlayView) {
        clearOverlay();
        this.overlayView = overlayView;
        addView(overlayView);
    }

    public void clearOverlay() {
        removeView(overlayView);
        this.overlayView = null;
    }

    public static FileRowView create(Context context, ViewGroup parent) {
        return (FileRowView) LayoutInflater.from(context).inflate(R.layout.filerow, parent, false);
    }
}
