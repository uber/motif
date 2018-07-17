package com.uber.motif.sample.app.root;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.uber.motif.Scope;
import com.uber.motif.sample.app.photolist.PhotoListScope;
import com.uber.motif.sample.app.photorow.PhotoClickListener;
import com.uber.motif.sample.app.photorow.PhotoLongClickListener;
import com.uber.motif.sample.lib.db.Database;
import com.uber.motif.sample.lib.db.RootDir;

@Scope
public interface RootScope {

    View view();

    PhotoListScope photoList(ViewGroup parent);

    abstract class Objects {

        public abstract Database database();
        public abstract RootDir rootDir();
        public abstract PhotoLongClickListener photoLongClickListener(RootController controller);
        public abstract PhotoClickListener photoClickListener(RootController controller);

        abstract RootController controller();

        View view(RootController controller) {
            return controller.getView();
        }
    }

    interface Parent {

        Context context();
    }
}
