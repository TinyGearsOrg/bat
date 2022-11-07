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

package org.tinygears.bat.classfile.constant.editor

import org.tinygears.bat.classfile.constant.Constant
import java.util.IdentityHashMap

internal class ConstantUsageMarker {
    private val usageMap: MutableMap<Constant, Usage> = IdentityHashMap()

    fun markUsed(item: Constant) {
        usageMap[item] = Usage.USED
    }

    fun isUsed(item: Constant): Boolean {
        return usageMap[item] == Usage.USED
    }
}

internal enum class Usage {
    USED
}