package com.uber.motif.sample.lib.photo;


import android.support.v7.util.DiffUtil;

import com.uber.motif.sample.lib.db.Photo;

public class PhotoDiffItemCallback extends DiffUtil.ItemCallback<Photo> {

    @Override
    public boolean areItemsTheSame(Photo oldItem, Photo newItem) {
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(Photo oldItem, Photo newItem) {
        return oldItem == newItem;
    }
}
