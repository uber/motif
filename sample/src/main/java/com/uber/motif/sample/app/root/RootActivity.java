package com.uber.motif.sample.app.root;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

public class RootActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = new RootScopeImpl(() -> RootActivity.this).view();
        setContentView(rootView);
    }
}
