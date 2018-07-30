package motif.sample.app.photo_grid_item;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding2.view.RxView;
import com.uber.motif.sample.R;
import motif.sample.lib.db.Photo;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public class PhotoGridItemView extends FrameLayout {

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.touch)
    View touchView;

    @Nullable
    private View overlayView;

    public PhotoGridItemView(Context context) {
        super(context);
    }

    public PhotoGridItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoGridItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    Observable<Object> clicks() {
        return RxView.clicks(touchView);
    }

    void setPhoto(Photo photo) {
        clearOverlay();
        Glide.with(this)
                .load(photo.location)
                .thumbnail(0.1f)
                .into(imageView);
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected && !isSelected()) {
            showOverlay();
        } else if (!selected && isSelected()){
            clearOverlay();
        }
    }

    @Override
    public boolean isSelected() {
        return overlayView != null;
    }

    private void showOverlay() {
        clearOverlay();
        this.overlayView = createOverlay();
        addView(overlayView);
    }

    private void clearOverlay() {
        removeView(overlayView);
        this.overlayView = null;
    }

    private View createOverlay() {
        return LayoutInflater.from(getContext()).inflate(R.layout.photo_grid_item_overlay, this, false);
    }

    public static PhotoGridItemView create(ViewGroup parent) {
        return (PhotoGridItemView) LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_grid_item, parent, false);
    }
}
