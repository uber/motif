package com.uber.motif.sample.app.root;


import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import com.uber.motif.sample.app.photo_grid.PhotoGridView;
import com.uber.motif.sample.app.bottom_sheet.BottomSheetView;

public class RootView extends CoordinatorLayout {

    private BottomSheetView bottomSheetView;

    public RootView(Context context) {
        this(context, null);
    }

    public RootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void showPhotos(PhotoGridView view) {
        addView(view);
    }

    public boolean isSectionViewShowing() {
        return bottomSheetView != null;
    }

    public void showBottomSheet(BottomSheetView view) {
        clearBottomSheet();
        bottomSheetView = view;
        addView(bottomSheetView);
    }

    public void clearBottomSheet() {
        if (bottomSheetView != null) {
            removeView(bottomSheetView);
            bottomSheetView = null;
        }
    }
}
