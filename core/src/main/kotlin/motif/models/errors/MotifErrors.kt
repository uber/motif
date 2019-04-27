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
package motif.models.errors

class MotifErrors(
        private val parsingErrors: List<MotifError>,
        private val scopeCycleError: ScopeCycleError?,
        private val missingDependenciesErrors: List<MissingDependenciesError>,
        private val dependencyCycleErrors: List<DependencyCycleError>,
        private val duplicateFactoryMethodsErrors: List<DuplicateFactoryMethodsError>,
        private val notExposedErrors: List<NotExposedError>,
        private val notExposedDynamicErrors: List<NotExposedDynamicError>)
    : List<MotifError> by parsingErrors +
        notExposedErrors +
        notExposedDynamicErrors +
        listOfNotNull(scopeCycleError) +
        duplicateFactoryMethodsErrors +
        dependencyCycleErrors +
        missingDependenciesErrors {

    override fun toString(): String {
        return toList().toString()
    }
}