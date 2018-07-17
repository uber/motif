package com.uber.motif.sample.lib.controller;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jakewharton.rxbinding2.view.RxView;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import io.reactivex.subjects.MaybeSubject;

public class Controller<V extends View> {

    private static final Object DISPOSE = new Object();

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
    private final MaybeSubject<Object> disposeMaybe = MaybeSubject.create();

    private State state = State.UNATTACHED;

    public Controller(
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

    protected void clicks(View view, View.OnClickListener listener) {
        RxView.clicks(view).as(autoDispose()).subscribe(o -> listener.onClick(view));
    }

    protected void longClicks(View view, View.OnLongClickListener listener) {
        RxView.longClicks(view).as(autoDispose()).subscribe(o -> listener.onLongClick(view));
    }

    public void attach() {
        if (state != State.UNATTACHED) return;
        onAttach();
        state = State.ATTACHED;
    }

    public <T> AutoDisposeConverter<T> autoDispose() {
        return AutoDispose.autoDisposable(disposeMaybe);
    }

    public void detach() {
        if (state != State.ATTACHED) return;
        onDetach();
        disposeMaybe.onSuccess(DISPOSE);
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
