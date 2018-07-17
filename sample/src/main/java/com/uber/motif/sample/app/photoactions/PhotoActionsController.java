package com.uber.motif.sample.app.photoactions;

import android.content.Context;
import android.view.ViewGroup;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.R;

public class PhotoActionsController extends Controller<PhotoActionsView> {

    public PhotoActionsController(
            Context context,
            ViewGroup parent) {
        super(context, parent, R.layout.photoactions);
    }
}
