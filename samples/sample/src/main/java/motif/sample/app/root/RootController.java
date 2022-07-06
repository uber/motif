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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.List;
import motif.sample.R;
import motif.sample.app.bottom_sheet.BottomSheetView;
import motif.sample.app.photo_grid.PhotoGridView;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.db.Database;
import motif.sample.lib.db.Photo;
import motif.sample.lib.multiselect.MultiSelector;

class RootController extends Controller<RootView> {

  private final RootScope scope;
  private final AppCompatActivity activity;
  private final Database database;
  private final MultiSelector multiSelector;
  private final ActivityResultLauncher<String> sdCardPermissionLauncher;

  RootController(
      RootScope scope, AppCompatActivity activity, Database database, MultiSelector multiSelector) {
    super(activity, R.layout.root);
    this.scope = scope;
    this.activity = activity;
    this.database = database;
    this.multiSelector = multiSelector;
    this.sdCardPermissionLauncher = register(activity);
  }

  /**
   * It has to be registered prior to the onResume lifecycle per official doc. More refer to
   * https://stackoverflow.com/a/64477786
   */
  private ActivityResultLauncher<String> register(AppCompatActivity activity) {
    return activity.registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), this::onSdCardPermissionReturned);
  }

  @Override
  protected void onAttach() {
    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermission();
    } else {
      loadData();
    }
  }

  @RequiresPermission("android.permission.READ_EXTERNAL_STORAGE")
  private void loadData() {
    database.populateIfNecessary().as(autoDispose()).subscribe(this::onDataPopulated);

    multiSelector.selected().as(autoDispose()).subscribe(this::onPhotoSelected);
  }

  private void onPhotoSelected(List<Photo> photos) {
    if (photos.isEmpty()) {
      view.clearBottomSheet();
    } else if (!view.isSectionViewShowing()) {
      BottomSheetView bottomSheetView = scope.bottomSheet(this.view).view();
      view.showBottomSheet(bottomSheetView);
    }
  }

  private void onDataPopulated() {
    PhotoGridView photoGridView = scope.photoList(view).view();
    view.showPhotos(photoGridView);
  }

  private void requestPermission() {
    sdCardPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
  }

  @SuppressLint("MissingPermission")
  private void onSdCardPermissionReturned(boolean granted) {
    loadData();
  }
}
