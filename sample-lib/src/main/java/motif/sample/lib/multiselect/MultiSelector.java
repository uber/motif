package motif.sample.lib.multiselect;


import com.jakewharton.rxrelay2.BehaviorRelay;
import motif.sample.lib.db.Photo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public class MultiSelector {

    private final BehaviorRelay<List<Photo>> selected = BehaviorRelay.createDefault(Collections.emptyList());

    public void clearSelected() {
        selected.accept(Collections.emptyList());
    }

    public void select(Photo photo) {
        List<Photo> value = update();
        value.add(photo);
        selected.accept(value);
    }

    public void deselect(Photo photo) {
        List<Photo> value = update();
        value.remove(photo);
        selected.accept(value);
    }

    public boolean isSelected(Photo photo) {
        return selected.getValue().contains(photo);
    }

    public Observable<List<Photo>> selected() {
        return selected;
    }

    private ArrayList<Photo> update() {
        return new ArrayList<>(selected.getValue());
    }
}
