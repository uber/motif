package motif.daggercomparison.dagger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import motif.sample.R;

public class LoggedInView extends FrameLayout {

    public LoggedInView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoggedInView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static LoggedInView create(ViewGroup parent) {
        return (LoggedInView) LayoutInflater.from(parent.getContext()).inflate(R.layout.root, parent, false);
    }
}
