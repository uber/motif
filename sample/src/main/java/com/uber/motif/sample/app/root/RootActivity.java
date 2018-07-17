package com.uber.motif.sample.app.root;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

public class RootActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = new RootScopeImpl(new RootScope.Parent() {
            @Override
            public Context context() {
                return RootActivity.this;
            }
        }).view();
        setContentView(rootView);
    }
}
