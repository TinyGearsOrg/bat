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
 * A constant representing a CONSTANT_Dynamic_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.10">CONSTANT_Dynamic_info Structure</a>
 */
class DynamicConstant private constructor(bootstrapMethodAttrIndex: Int = -1,
                                          nameAndTypeIndex:         Int = -1) : BootstrapRefConstant(bootstrapMethodAttrIndex, nameAndTypeIndex) {

    override val type: ConstantType
        get() = ConstantType.DYNAMIC

    fun copyWith(bootstrapMethodAttrIndex: Int = this.bootstrapMethodAttrIndex, nameAndTypeIndex: Int = this.nameAndTypeIndex): DynamicConstant {
        return DynamicConstant(bootstrapMethodAttrIndex, nameAndTypeIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitDynamicConstant(classFile, index, this)
    }

    override fun toString(): String {
        return "DynamicConstant[#$bootstrapMethodAttrIndex,#$nameAndTypeIndex]"
    }

    companion object {
        internal fun empty(): DynamicConstant {
            return DynamicConstant()
        }

        fun of(bootstrapMethodAttrIndex: Int, nameAndTypeIndex: Int): DynamicConstant {
            require(bootstrapMethodAttrIndex >= 0) { "bootstrapMethodAttrIndex must not be negative" }
            require(nameAndTypeIndex >= 1) { "nameAndTypeIndex must be a positive number" }
            return DynamicConstant(bootstrapMethodAttrIndex, nameAndTypeIndex)
        }
    }
}