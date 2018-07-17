package com.uber.controller;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private State state = State.UNATTACHED;

    public Controller(
            Context context,
            @Nullable ViewGroup parent,
            @LayoutRes int layout) {
        this(Controller.<V>inflate(context, parent, layout));
    }

    public Controller(V view) {
        this.view = view;
        view.addOnAttachStateChangeListener(attachListener);
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

    public void detach() {
        if (state != State.ATTACHED) return;
        onDetach();
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
