package com.uber.motif.sample.filelist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.uber.motif.sample.filerow.FileRowController;
import com.uber.motif.sample.filerow.FileRowView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

    private final FileListScope scope;

    private List<File> files = new ArrayList<>();

    public FileListAdapter(FileListScope scope) {
        this.scope = scope;
    }

    public void setFiles(List<File> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FileRowView view = FileRowView.create(parent.getContext(), parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.unbind();
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final FileRowView view;

        @Nullable private FileRowController controller;

        ViewHolder(FileRowView view) {
            super(view);
            this.view = view;
        }

        void bind(int position) {
            unbind();
            controller = scope.fileRow(view, files.get(position)).controller();
        }

        void unbind() {
            if (controller != null) {
                controller.detach();
            }
        }
    }
}
