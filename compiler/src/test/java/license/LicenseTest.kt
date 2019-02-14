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
package license

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LicenseTest {

    @Rule @JvmField val temporaryFolder = TemporaryFolder()

    private val modules = sequenceOf(
            "lib",
            "compiler",
            "samples/sample",
            "samples/sample-kotlin",
            "samples/sample-lib",
            "samples/dagger-comparison",
            "plugin",
            "ir",
            "it",
            "stub-compiler")

    private val licenseText = """
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
    """.trimIndent()

    @Test
    fun test() {
        val missingLicenses = modules.flatMap { File("../$it/src").walk() }
                .filter { it.extension == "java" || it.extension == "kt" }
                .filter { !it.ensureLicense() }
                .toList()
        assertThat(missingLicenses).isEmpty()
    }

    private fun File.ensureLicense(): Boolean {
        val hasLicense = useLines {
            it.take(3).find { "Copyright" in it } != null
        }

        if (hasLicense) {
            return true
        }

        val tmpFile = temporaryFolder.newFile()
        tmpFile.writer().use { out ->
            out.write(licenseText)
            out.appendln()
            reader().copyTo(out)
        }
        tmpFile.renameTo(this)
        return false
    }
}