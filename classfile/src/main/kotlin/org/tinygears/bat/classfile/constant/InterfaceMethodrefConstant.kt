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
 * A constant representing a CONSTANT_InterfaceMethodref_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.2">CONSTANT_InterfaceMethodref_info Structure</a>
 */
class InterfaceMethodrefConstant private constructor(classIndex:       Int = -1,
                                                     nameAndTypeIndex: Int = -1) : RefConstant(classIndex, nameAndTypeIndex) {

    override val type: ConstantType
        get() = ConstantType.INTERFACE_METHOD_REF

    fun copyWith(classIndex: Int = this.classIndex, nameAndTypeIndex: Int = this.nameAndTypeIndex): InterfaceMethodrefConstant {
        return InterfaceMethodrefConstant(classIndex, nameAndTypeIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitInterfaceMethodRefConstant(classFile, index, this)
    }

    override fun toString(): String {
        return "InterfaceMethodrefConstant[#$classIndex,#$nameAndTypeIndex]"
    }

    companion object {
        internal fun empty(): InterfaceMethodrefConstant {
            return InterfaceMethodrefConstant()
        }

        fun of(classIndex: Int, nameAndTypeIndex: Int): InterfaceMethodrefConstant {
            require(classIndex >= 1) { "classIndex must be a positive number" }
            require(nameAndTypeIndex >= 1) { "nameAndTypeIndex must be a positive number" }
            return InterfaceMethodrefConstant(classIndex, nameAndTypeIndex)
        }
    }
}