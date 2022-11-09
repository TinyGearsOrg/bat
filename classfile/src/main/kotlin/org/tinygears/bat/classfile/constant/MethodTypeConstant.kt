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
 * A constant representing a CONSTANT_MethodType_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.9">CONSTANT_MethodType_info Structure</a>
 */
class MethodTypeConstant private constructor(descriptorIndex: Int = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.METHOD_TYPE

    var descriptorIndex: Int = descriptorIndex
        private set

    fun getDescriptor(classFile: ClassFile): String {
        return classFile.getString(descriptorIndex)
    }

    fun copyWith(descriptorIndex: Int = this.descriptorIndex): MethodTypeConstant {
        return MethodTypeConstant(descriptorIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        descriptorIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(descriptorIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitMethodTypeConstant(classFile, index, this)
    }

    fun descriptorConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(descriptorIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::descriptorIndex))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MethodTypeConstant) return false

        return descriptorIndex == other.descriptorIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(descriptorIndex)
    }

    override fun toString(): String {
        return "MethodTypeConstant[#$descriptorIndex]"
    }

    companion object {
        internal fun empty(): MethodTypeConstant {
            return MethodTypeConstant()
        }

        fun of(descriptorIndex: Int): MethodTypeConstant {
            require(descriptorIndex >= 1) { "descriptorIndex must be a positive number" }
            return MethodTypeConstant(descriptorIndex)
        }
    }
}