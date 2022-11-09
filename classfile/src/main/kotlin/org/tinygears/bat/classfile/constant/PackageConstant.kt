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
 * A constant representing a CONSTANT_Package_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.12">CONSTANT_Package_info Structure</a>
 */
class PackageConstant private constructor(nameIndex: Int = -1): Constant() {

    override val type: ConstantType
        get() = ConstantType.PACKAGE

    var nameIndex: Int = nameIndex
        private set

    fun getPackageName(classFile: ClassFile): String {
        return classFile.getString(nameIndex)
    }

    fun copyWith(nameIndex: Int): PackageConstant {
        return PackageConstant(nameIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        nameIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(nameIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitPackageConstant(classFile, index, this)
    }

    fun nameConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(nameIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::nameIndex))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PackageConstant) return false

        return nameIndex == other.nameIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(nameIndex)
    }

    override fun toString(): String {
        return "PackageConstant[#$nameIndex]"
    }

    companion object {
        internal fun empty(): PackageConstant {
            return PackageConstant()
        }

        fun of(nameIndex: Int): PackageConstant {
            require(nameIndex >= 1) { "nameIndex must be a positive number" }
            return PackageConstant(nameIndex)
        }
    }
}