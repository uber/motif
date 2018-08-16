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
package motif.sample.lib.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class ControllerAdapter<T, V extends View> extends ListAdapter<T, ControllerAdapter.ViewHolder> {

    private final Factory<T, V> factory;

    public ControllerAdapter(Factory<T, V> factory, DiffUtil.ItemCallback<T> diffItemCallback) {
        super(diffItemCallback);
        this.factory = factory;
    }

    @NonNull
    @Override
    public ControllerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        V view = factory.view(parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ControllerAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final V view;

        @Nullable private T item;
        @Nullable private Controller controller;

        ViewHolder(V view) {
            super(view);
            this.view = view;
        }

        void bind(int position) {
            unbind();
            item = getItem(position);
            controller = factory.controller(view, item);
            controller.attach();
        }

        void unbind() {
            if (controller != null) {
                controller.detach();
            }

            item = null;
            controller = null;
        }
    }

    public interface Factory<T, V extends View> {

        Controller controller(V view, T item);

        V view(ViewGroup parent);
    }
}
