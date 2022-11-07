/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tinygears.bat.dexfile.value

import org.tinygears.bat.dexfile.value.AnnotationElement.Companion.of
import org.tinygears.bat.dexfile.value.EncodedAnnotationValue.Companion.of

class EncodedAnnotationValueTest : EncodedValueTest<EncodedAnnotationValue>() {
    override val testInstance: Array<EncodedAnnotationValue>
        get() = arrayOf(
            of(1, of(2, EncodedStringValue.of(3))),
            of(
                65535,
                of(65535, EncodedStringValue.of(65535)),
                of(1, EncodedIntValue.of(1))
            )
        )
}