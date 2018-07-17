package com.uber.motif.sample.fileactions;

import android.content.Context;
import android.view.ViewGroup;
import com.uber.controller.Controller;
import com.uber.motif.sample.R;

public class FileActionsController extends Controller<FileActionsView> {

    public FileActionsController(
            Context context,
            ViewGroup parent) {
        super(context, parent, R.layout.fileactions);
    }
}
