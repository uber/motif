package motif.sample.app.bottom_header;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import motif.sample.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BottomHeaderView extends FrameLayout {

    @BindView(R.id.title)
    TextView titleView;

    @BindView(R.id.cancel)
    View cancelButton;

    @BindView(R.id.close)
    View closeButton;

    @BindView(R.id.edit)
    View editButton;

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
        ButterKnife.bind(this);

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
