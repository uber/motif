package motif.sample.lib.bottom_header;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import motif.sample_lib.R;

import java.util.Locale;

public class BottomHeaderView extends FrameLayout {

    private TextView titleView;
    private View cancelButton;
    private View closeButton;
    private View editButton;

    private Listener listener;

    public BottomHeaderView(@NonNull Context context) {
        this(context, null);
    }

    public BottomHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomHeaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setOpenFraction(@FloatRange(from = 0, to = 1) float f) {
        closeButton.setAlpha(f);
        editButton.setAlpha(1 - f);
    }

    public void setSelectedCount(int count) {
        String itemText = count == 1 ? "Item" : "Items";
        String title = String.format(Locale.getDefault(), "%d %s Selected", count, itemText);
        titleView.setText(title);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        titleView = findViewById(R.id.title);
        cancelButton = findViewById(R.id.cancel);
        closeButton = findViewById(R.id.close);
        editButton = findViewById(R.id.edit);

        cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
        });
    }

    public interface Listener {

        void onCancel();
    }
}
