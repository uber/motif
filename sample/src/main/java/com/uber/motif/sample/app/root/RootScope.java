package com.uber.motif.sample.app.root;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.uber.motif.Scope;
import com.uber.motif.sample.app.filelist.FileListScope;
import com.uber.motif.sample.app.filerow.FileClickListener;
import com.uber.motif.sample.app.filerow.FileLongClickListener;
import com.uber.motif.sample.model.RootDir;

@Scope
public interface RootScope {

    View view();

    FileListScope fileList(ViewGroup parent);

    abstract class Objects {

        public abstract RootDir rootDir();
        public abstract FileLongClickListener fileLongClickListener(RootController controller);
        public abstract FileClickListener fileClickListener(RootController controller);

        abstract RootController controller();

        View view(RootController controller) {
            return controller.getView();
        }
    }

    interface Parent {

        Context context();
    }
}
