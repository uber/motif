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

package motif.compiler.buck;

import javax.annotation.Nullable;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks which classes are actually read by the compiler by providing a special {@link
 * JavaFileManager}.
 */
class ClassUsageTracker implements FileManagerListener {

  @Nullable private final File classpathDir;

  private final Set<String> classnames = new HashSet<>();

  public ClassUsageTracker(@Nullable File classpathDir) {
    this.classpathDir = classpathDir;
  }

  public Set<String> getClassnames() {
    return classnames;
  }

  @Override
  public void onFileRead(FileObject fileObject) {
    if (!(fileObject instanceof JavaFileObject) || classpathDir == null) {
      return;
    }
    JavaFileObject javaFileObject = (JavaFileObject) fileObject;

    URI uri = javaFileObject.toUri();
    if (!uri.getScheme().equals("file")) {
      return;
    }

    String absolutePath = new File(uri).getAbsolutePath();
    String classpathPath = classpathDir.getAbsolutePath();
    if (absolutePath.startsWith(classpathPath) && absolutePath.endsWith(".class")) {
      String classname = absolutePath
              .substring(0, absolutePath.indexOf(".class"))
              .substring(classpathPath.length() + 1)
              .replace(File.separatorChar, '.');
      classnames.add(classname);
    }
  }

  @Override
  public void onFileWritten(FileObject file) {}
}
