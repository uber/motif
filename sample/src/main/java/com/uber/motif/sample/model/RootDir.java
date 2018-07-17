package com.uber.motif.sample.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import java.io.File;

public class RootDir {

    private final File file;

    public RootDir(Context context) {
        this.file = ContextCompat.getDataDir(context);
    }

    public File getFile() {
        return file;
    }
}
