package com.uber.motif.sample.app.photorow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.uber.motif.sample.R;

public class PhotoRowView extends FrameLayout {

    @Nullable
    private View overlayView;

    public PhotoRowView(Context context) {
        super(context);
    }

    public PhotoRowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoRowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

    public static PhotoRowView create(Context context, ViewGroup parent) {
        return (PhotoRowView) LayoutInflater.from(context).inflate(R.layout.photorow, parent, false);
    }
}
