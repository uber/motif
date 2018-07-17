package com.uber.motif.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class RootActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root);

        String a = new RootScopeImpl(new RootScopeImpl.Parent() {
        }).a();

        ((TextView) findViewById(R.id.text)).setText(a);
    }
}
