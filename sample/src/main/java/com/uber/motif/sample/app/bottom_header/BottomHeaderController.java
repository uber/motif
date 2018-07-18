package com.uber.motif.sample.app.bottom_header;

import android.view.ViewGroup;

import com.uber.motif.sample.R;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.lib.multiselect.MultiSelector;

public class BottomHeaderController extends Controller<BottomHeaderView> {

    private final MultiSelector multiSelector;

    public BottomHeaderController(
            ViewGroup parent,
            MultiSelector multiSelector) {
        super(parent, R.layout.bottomheader);
        this.multiSelector = multiSelector;
    }

    @Override
    protected void onAttach() {
        view.setListener(multiSelector::clearSelected);

        multiSelector.selected()
                .as(autoDispose())
                .subscribe(photos -> {
                    view.setSelectedCount(photos.size());
                });
    }
}
