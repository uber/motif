package com.uber.motif.sample.app.photolist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.uber.motif.sample.app.photorow.PhotoRowController;
import com.uber.motif.sample.app.photorow.PhotoRowView;
import com.uber.motif.sample.app.photorow.PhotoTouches;
import com.uber.motif.sample.lib.db.Photo;
import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> implements PhotoTouches {

    private final PhotoListScope scope;
    private final Relay<Photo> photoTouches = PublishRelay.create();

    private List<Photo> photos = new ArrayList<>();

    public PhotoAdapter(PhotoListScope scope) {
        this.scope = scope;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    @Override
    public Observable<Photo> touches() {
        return photoTouches;
    }

    @NonNull
    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PhotoRowView view = PhotoRowView.create(parent.getContext(), parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final PhotoRowView view;

        @Nullable private Photo photo;
        @Nullable private PhotoRowController controller;

        ViewHolder(PhotoRowView view) {
            super(view);
            this.view = view;

            view.setOnTouchListener((v, event) -> {
                if (photo != null) {
                    photoTouches.accept(photo);
                }
                return false;
            });
        }

        void bind(int position) {
            unbind();
            photo = photos.get(position);
            controller = scope.photoRow(view, photo).controller();
            controller.attach();
        }

        void unbind() {
            if (controller != null) {
                controller.detach();
            }

            photo = null;
            controller = null;
        }
    }
}
