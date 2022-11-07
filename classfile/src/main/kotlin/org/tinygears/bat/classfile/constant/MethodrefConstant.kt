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
package org.tinygears.bat.classfile.constant

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.constant.visitor.ConstantVisitor

/**
 * A constant representing a CONSTANT_Methodref_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.2">CONSTANT_MethodRef_info Structure</a>
 */
data class MethodrefConstant private constructor(override var _classIndex:       Int = -1,
                                                 override var _nameAndTypeIndex: Int = -1) : RefConstant(_classIndex, _nameAndTypeIndex) {

    override val type: ConstantType
        get() = ConstantType.METHOD_REF

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitMethodRefConstant(classFile, index, this)
    }

    companion object {
        internal fun empty(): MethodrefConstant {
            return MethodrefConstant()
        }

        fun of(classIndex: Int, nameAndTypeIndex: Int): MethodrefConstant {
            require(classIndex >= 1) { "classIndex must be a positive number" }
            require(nameAndTypeIndex >= 1) { "nameAndTypeIndex must be a positive number" }
            return MethodrefConstant(classIndex, nameAndTypeIndex)
        }
    }
}