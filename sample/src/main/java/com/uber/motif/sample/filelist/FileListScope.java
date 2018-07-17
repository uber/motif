package com.uber.motif.sample.filelist;

import com.uber.motif.Scope;
import com.uber.motif.sample.filerow.FileRowScope;
import com.uber.motif.sample.filerow.FileRowView;
import com.uber.motif.sample.filerow.FileTouches;

import java.io.File;

@Scope
public interface FileListScope {

    FileListView view();

    FileRowScope fileRow(FileRowView view, File file);

    abstract class Objects {

        public abstract FileTouches fileTouches(FileListAdapter adapter);

        abstract FileListAdapter adapter();
        abstract FileListController controller();

        FileListView view(FileListController controller) {
            return controller.getView();
        }
    }
}
