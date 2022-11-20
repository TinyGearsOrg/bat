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

package org.tinygears.bat.classfile.evaluation.value

import org.tinygears.bat.classfile.attribute.preverification.UninitializedThisVariable
import org.tinygears.bat.classfile.attribute.preverification.UninitializedVariable
import org.tinygears.bat.classfile.attribute.preverification.VerificationType
import org.tinygears.bat.classfile.constant.editor.ConstantPoolEditor
import org.tinygears.bat.util.JvmType

class UninitializedReferenceValue constructor(override val type: JvmType, val offset: Int): ReferenceValue {

    override val isNull: Boolean
        get() = false

    val isUninitializedThis: Boolean
        get() = offset < 0

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return if (isUninitializedThis) {
            UninitializedThisVariable
        } else {
            UninitializedVariable.of(offset)
        }
    }

    override fun toString(): String {
        return if (isUninitializedThis) {
            "uninitializedThis($type)"
        } else {
            "uninitialized($type, $offset)"
        }
    }
}