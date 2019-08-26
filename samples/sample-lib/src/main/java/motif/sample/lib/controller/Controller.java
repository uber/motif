/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;

import io.reactivex.subjects.CompletableSubject;

public class Controller<V extends View> {

    private final View.OnAttachStateChangeListener attachListener = new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(View v) {
            attach();
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            v.removeOnAttachStateChangeListener(this);
            detach();
        }
    };

    protected final V view;
    private final CompletableSubject disposeCompletable = CompletableSubject.create();

    private State state = State.UNATTACHED;

    public Controller(
            ViewGroup parent,
            @LayoutRes int layout) {
        this(Controller.inflate(parent.getContext(), parent, layout), true);
    }

    public Controller(
            Context context,
            @LayoutRes int layout) {
        this(Controller.inflate(context, null, layout), true);
    }

    private Controller(
            Context context,
            @Nullable ViewGroup parent,
            @LayoutRes int layout) {
        this(Controller.inflate(context, parent, layout), true);
    }

    public Controller(V view, boolean autoAttach) {
        this.view = view;
        if (autoAttach) {
            view.addOnAttachStateChangeListener(attachListener);
        }
    }

    public V getView() {
        return view;
    }

    protected void onAttach() {}

    protected void onDetach() {}

    public void attach() {
        if (state != State.UNATTACHED) return;
        onAttach();
        state = State.ATTACHED;
    }

    public <T> AutoDisposeConverter<T> autoDispose() {
        return AutoDispose.autoDisposable(disposeCompletable);
    }

    public void detach() {
        if (state != State.ATTACHED) return;
        onDetach();
        disposeCompletable.onComplete();
        state = State.DETACHED;
    }

    @SuppressWarnings("unchecked")
    private static <V> V inflate(
            Context context,
            @Nullable ViewGroup parent,
            @LayoutRes int layout) {
        if (parent == null) {
            return (V) LayoutInflater.from(context).inflate(layout, null);
        } else {
            return (V) LayoutInflater.from(context).inflate(layout, parent, false);
        }
    }

    private enum State {
        UNATTACHED,
        ATTACHED,
        DETACHED
    }
}
