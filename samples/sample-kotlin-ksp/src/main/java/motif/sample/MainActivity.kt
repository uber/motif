/*
 * Copyright (c) 2022 Uber Technologies, Inc.
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
package motif.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import motif.ScopeFactory

class MainActivity : Activity() {

  private val mainScope: MainScope =
      ScopeFactory.create(MainScope::class.java, object : MainScope.Dependencies {})

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val greeter = mainScope.greeter()

    findViewById<TextView>(R.id.text).text = greeter.greet()
  }
}
