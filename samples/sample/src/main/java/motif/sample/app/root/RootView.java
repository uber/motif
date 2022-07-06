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
package motif.sample.app.root;

import android.content.Context;
import android.util.AttributeSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import javax.inject.Inject;
import motif.sample.app.bottom_sheet.BottomSheetView;
import motif.sample.app.photo_grid.PhotoGridView;

public class RootView extends CoordinatorLayout {

  private BottomSheetView bottomSheetView;

  @Inject
  public RootView(Context context) {
    this(context, null);
  }

  public RootView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RootView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void showPhotos(PhotoGridView view) {
    addView(view);
  }

  public boolean isSectionViewShowing() {
    return bottomSheetView != null;
  }

  public void showBottomSheet(BottomSheetView view) {
    clearBottomSheet();
    bottomSheetView = view;
    addView(bottomSheetView);
  }

  public void clearBottomSheet() {
    if (bottomSheetView != null) {
      removeView(bottomSheetView);
      bottomSheetView = null;
    }
  }
}
