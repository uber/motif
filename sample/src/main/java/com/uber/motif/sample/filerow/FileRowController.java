package com.uber.motif.sample.filerow;

import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.uber.controller.Controller;
import com.uber.motif.sample.R;
import com.uber.motif.sample.fileactions.FileActionsView;

import java.io.File;

public class FileRowController extends Controller<FileRowView> {

    @BindView(R.id.name)
    TextView nameView;

    private final FileRowScope scope;
    private final File file;
    private final FileTouches fileTouches;
    private final FileClickListener fileClickListener;
    private final FileLongClickListener fileLongClickListener;

    public FileRowController(
            FileRowScope scope,
            FileRowView view,
            File file,
            FileTouches fileTouches,
            FileClickListener fileClickListener,
            FileLongClickListener fileLongClickListener) {
        super(view, false);
        this.scope = scope;
        this.file = file;
        this.fileTouches = fileTouches;
        this.fileClickListener = fileClickListener;
        this.fileLongClickListener = fileLongClickListener;
        ButterKnife.bind(this, view);
    }

    @Override
    protected void onAttach() {
        nameView.setText(file.toString());

        clicks(view, v -> fileClickListener.onClick(file));

        longClicks(view, v -> {
            fileLongClickListener.onLongClick(file);
            FileActionsView actionsView = scope.actions(getView()).view();
            getView().setOverlay(actionsView);
            return true;
        });

        fileTouches.touches()
                .filter(touchedFile -> !touchedFile.equals(file))
                .as(autoDispose())
                .subscribe(touchedFile -> getView().clearOverlay());
    }
}
