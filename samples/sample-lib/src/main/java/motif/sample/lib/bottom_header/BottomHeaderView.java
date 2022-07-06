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
package motif.sample.lib.bottom_header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;
import javax.inject.Inject;
import motif.sample_lib.R;

public class BottomHeaderView extends FrameLayout {

  private TextView titleView;
  private View cancelButton;
  private View closeButton;
  private View editButton;

  private Listener listener;

  @Inject
  public BottomHeaderView(@NonNull Context context) {
    this(context, null);
  }

  public BottomHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BottomHeaderView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void setOpenFraction(@FloatRange(from = 0, to = 1) float f) {
    closeButton.setAlpha(f);
    editButton.setAlpha(1 - f);
  }

  public void setSelectedCount(int count) {
    String itemText = count == 1 ? "Item" : "Items";
    String title = String.format(Locale.getDefault(), "%d %s Selected", count, itemText);
    titleView.setText(title);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    titleView = findViewById(R.id.title);
    cancelButton = findViewById(R.id.cancel);
    closeButton = findViewById(R.id.close);
    editButton = findViewById(R.id.edit);

    cancelButton.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.onCancel();
          }
        });
  }

  public interface Listener {

    void onCancel();
  }
}
