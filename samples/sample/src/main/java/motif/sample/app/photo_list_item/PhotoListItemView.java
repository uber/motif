/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.sample.app.photo_list_item;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import motif.sample.R;
import motif.sample.lib.db.Photo;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoListItemView extends FrameLayout {

    @BindView(R.id.image)
    ImageView imageView;

    public PhotoListItemView(@NonNull Context context) {
        this(context, null);
    }

    public PhotoListItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoListItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPhoto(Photo photo) {
        Glide.with(this)
                .load(photo.location)
                .thumbnail(0.1f)
                .into(imageView);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public static PhotoListItemView create(ViewGroup parent) {
        return (PhotoListItemView) LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
    }
}
