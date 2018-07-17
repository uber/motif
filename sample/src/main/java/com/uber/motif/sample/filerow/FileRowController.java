package com.uber.motif.sample.filerow;

import android.view.View;
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
    private final FileClickListener fileClickListener;
    private final FileLongClickListener fileLongClickListener;

    public FileRowController(
            FileRowScope scope,
            FileRowView view,
            File file,
            FileClickListener fileClickListener,
            FileLongClickListener fileLongClickListener) {
        super(view);
        this.scope = scope;
        this.fileClickListener = fileClickListener;
        this.fileLongClickListener = fileLongClickListener;
        this.file = file;
        ButterKnife.bind(this, view);
    }

    @Override
    protected void onAttach() {
        nameView.setText(file.toString());
        // TODO Switch to rx view clicks so we can easily clean up click listeners when we recycle.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileClickListener.onClick(file);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fileLongClickListener.onLongClick(file);
                FileActionsView actionsView = scope.actions(getView()).view();
                getView().setOverlay(actionsView);
                return true;
            }
        });
    }
}
