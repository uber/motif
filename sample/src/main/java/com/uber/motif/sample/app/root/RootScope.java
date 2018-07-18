package com.uber.motif.sample.app.root;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.uber.motif.Scope;
import com.uber.motif.sample.app.photo_grid.PhotoGridScope;
import com.uber.motif.sample.app.bottom_sheet.BottomSheetScope;
import com.uber.motif.sample.lib.db.Database;
import com.uber.motif.sample.lib.db.RootDir;
import com.uber.motif.sample.lib.multiselect.MultiSelector;

@Scope
public interface RootScope {

    View view();

    PhotoGridScope photoList(ViewGroup parent);

    BottomSheetScope bottomSheet(ViewGroup parent);

    abstract class Objects {

        public abstract Database database();
        public abstract RootDir rootDir();
        public abstract MultiSelector multiSelector();

        abstract RootController controller();

        View view(RootController controller) {
            return controller.getView();
        }
    }

    interface Parent {

        Context context();
    }
}
