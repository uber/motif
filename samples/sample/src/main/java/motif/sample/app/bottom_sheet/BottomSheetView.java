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
package motif.sample.app.bottom_sheet;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import javax.inject.Inject;
import motif.sample.app.photo_list.PhotoListView;
import motif.sample.lib.bottom_header.BottomHeaderView;

public class BottomSheetView extends LinearLayout {

  private BottomSheetBehavior<BottomSheetView> behavior;

  @Inject
  public BottomSheetView(@NonNull Context context) {
    this(context, null);
  }

  public BottomSheetView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BottomSheetView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    getViewTreeObserver()
        .addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
              @Override
              public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                setTranslationY(behavior.getPeekHeight());
                animate()
                    .translationY(0)
                    .setDuration(100)
                    .setInterpolator(new LinearOutSlowInInterpolator())
                    .start();
                return true;
              }
            });
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    behavior = BottomSheetBehavior.from(this);
  }

  public void showHeader(BottomHeaderView view) {
    addView(view);
    view.setOpenFraction(0);

    behavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {}

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            view.setOpenFraction(slideOffset);
          }
        });
    view.setOnClickListener(
        v -> {
          int nextState = behavior.getState() == STATE_EXPANDED ? STATE_COLLAPSED : STATE_EXPANDED;
          behavior.setState(nextState);
        });
  }

  public void showPhotoList(PhotoListView view) {
    addView(view);
  }
}
