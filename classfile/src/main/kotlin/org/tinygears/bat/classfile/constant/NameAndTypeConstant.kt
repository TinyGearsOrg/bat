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
import org.tinygears.bat.classfile.constant.visitor.PropertyAccessor
import org.tinygears.bat.classfile.constant.visitor.ReferencedConstantVisitor
import org.tinygears.bat.classfile.io.ClassDataInput
import org.tinygears.bat.classfile.io.ClassDataOutput
import java.io.IOException
import java.util.*

/**
 * A constant representing a CONSTANT_NameAndType_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.6">CONSTANT_NameAndType_info Structure</a>
 */
class NameAndTypeConstant private constructor(nameIndex:       Int = -1,
                                              descriptorIndex: Int = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.NAME_AND_TYPE

    var nameIndex: Int = nameIndex
        private set

    var descriptorIndex: Int = descriptorIndex
        private set

    fun getMemberName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(descriptorIndex)
    }

    fun copyWith(nameIndex: Int = this.nameIndex, descriptorIndex: Int = this.descriptorIndex): NameAndTypeConstant {
        return NameAndTypeConstant(nameIndex, descriptorIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        nameIndex       = input.readUnsignedShort()
        descriptorIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(nameIndex)
        output.writeShort(descriptorIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitNameAndTypeConstant(classFile, index, this)
    }

    fun descriptorConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(descriptorIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::nameIndex))
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::descriptorIndex))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NameAndTypeConstant) return false

        return nameIndex       == other.nameIndex &&
               descriptorIndex == other.descriptorIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(nameIndex, descriptorIndex)
    }

    override fun toString(): String {
        return "NameAndTypeConstant[#$nameIndex,#$descriptorIndex]"
    }

    companion object {
        internal fun empty(): NameAndTypeConstant {
            return NameAndTypeConstant()
        }

        fun of(nameIndex: Int, descriptorIndex: Int): NameAndTypeConstant {
            require(nameIndex >= 1) { "nameIndex must be a positive number" }
            require(descriptorIndex >= 1) { "descriptorIndex must be a positive number" }
            return NameAndTypeConstant(nameIndex, descriptorIndex)
        }
    }
}