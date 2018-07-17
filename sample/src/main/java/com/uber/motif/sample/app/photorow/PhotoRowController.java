package com.uber.motif.sample.app.photorow;

import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.R;
import com.uber.motif.sample.app.photoactions.PhotoActionsView;
import com.uber.motif.sample.lib.db.Photo;

public class PhotoRowController extends Controller<PhotoRowView> {

    @BindView(R.id.name)
    TextView nameView;

    private final Photo photo;
    private final PhotoRowScope scope;
    private final PhotoTouches photoTouches;
    private final PhotoClickListener photoClickListener;
    private final PhotoLongClickListener photoLongClickListener;

    public PhotoRowController(
            PhotoRowScope scope,
            PhotoRowView view,
            Photo photo,
            PhotoTouches photoTouches,
            PhotoClickListener photoClickListener,
            PhotoLongClickListener photoLongClickListener) {
        super(view, false);
        this.scope = scope;
        this.photo = photo;
        this.photoTouches = photoTouches;
        this.photoClickListener = photoClickListener;
        this.photoLongClickListener = photoLongClickListener;
        ButterKnife.bind(this, view);
    }

    @Override
    protected void onAttach() {
        nameView.setText(photo.title);

        clicks(view, v -> photoClickListener.onClick(photo));

        longClicks(view, v -> {
            photoLongClickListener.onLongClick(photo);
            PhotoActionsView actionsView = scope.actions(getView()).view();
            getView().setOverlay(actionsView);
            return true;
        });

        photoTouches.touches()
                .filter(touchedPhoto -> !touchedPhoto.equals(photo))
                .as(autoDispose())
                .subscribe(o -> getView().clearOverlay());
    }
}
