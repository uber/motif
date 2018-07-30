package motif.sample.app.photo_list;


import android.view.ViewGroup;

import com.uber.motif.sample.R;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.multiselect.MultiSelector;

public class PhotoListController extends Controller<PhotoListView> {

    private final PhotoListAdapter adapter;
    private final MultiSelector multiSelector;

    public PhotoListController(ViewGroup parent, PhotoListAdapter adapter, MultiSelector multiSelector) {
        super(parent, R.layout.photo_list);
        this.adapter = adapter;
        this.multiSelector = multiSelector;
    }

    @Override
    protected void onAttach() {
        view.setAdapter(adapter);

        multiSelector.selected()
                .as(autoDispose())
                .subscribe(adapter::submitList);
    }
}
