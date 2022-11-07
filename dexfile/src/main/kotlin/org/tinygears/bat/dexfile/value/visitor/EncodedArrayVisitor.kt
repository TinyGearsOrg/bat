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

package org.tinygears.bat.dexfile.value.visitor

import org.tinygears.bat.dexfile.DexFile
import org.tinygears.bat.dexfile.value.EncodedArrayValue
import org.tinygears.bat.dexfile.value.EncodedValue

fun filterByStartIndex(startIndex: Int, visitor: EncodedValueVisitor): EncodedArrayVisitor {
    return EncodedArrayVisitor { dexFile, _, index, value ->
        if (index >= startIndex) {
            value.accept(dexFile, visitor)
        }
    }
}

fun interface EncodedArrayVisitor {
    fun visitEncodedValue(dexFile: DexFile, array: EncodedArrayValue, index: Int, value: EncodedValue)
}