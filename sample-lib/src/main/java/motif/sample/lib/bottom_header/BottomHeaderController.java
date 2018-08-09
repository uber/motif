package motif.sample.lib.bottom_header;

import android.view.ViewGroup;

import motif.sample.lib.controller.Controller;
import motif.sample.lib.multiselect.MultiSelector;
import motif.sample_lib.R;

public class BottomHeaderController extends Controller<BottomHeaderView> {

    private final MultiSelector multiSelector;

    public BottomHeaderController(
            ViewGroup parent,
            MultiSelector multiSelector) {
        super(parent, R.layout.bottomheader);
        this.multiSelector = multiSelector;
    }

    @Override
    protected void onAttach() {
        view.setListener(multiSelector::clearSelected);

        multiSelector.selected()
                .as(autoDispose())
                .subscribe(photos -> {
                    view.setSelectedCount(photos.size());
                });
    }
}
