package motif.daggercomparison.dagger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import motif.sample.R;

public class RootView extends FrameLayout {

    public RootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static RootView create(ViewGroup parent) {
        return (RootView) LayoutInflater.from(parent.getContext()).inflate(R.layout.root, parent, false);
    }
}
