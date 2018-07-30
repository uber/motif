package motif.sample.app.photo_grid;

import android.view.ViewGroup;

import com.uber.motif.sample.R;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.db.Database;

public class PhotoGridController extends Controller<PhotoGridView> {

    private final Database database;
    private final PhotoGridAdapter adapter;

    public PhotoGridController(
            ViewGroup parent,
            Database database,
            PhotoGridAdapter adapter) {
        super(parent, R.layout.photo_grid);
        this.database = database;
        this.adapter = adapter;
    }

    @Override
    protected void onAttach() {
        database.allPhotos().as(autoDispose())
                .subscribe(photos -> {
                    view.setAdapter(adapter);
                    adapter.submitList(photos);
                });
    }
}
