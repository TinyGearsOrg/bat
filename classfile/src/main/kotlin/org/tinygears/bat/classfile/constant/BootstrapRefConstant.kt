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
 * An abstract base class for constants referencing a bootstrap method,
 * i.e. Dynamic or InvokeDynamic.
 */
abstract class BootstrapRefConstant(bootstrapMethodAttrIndex: Int = -1,
                                    nameAndTypeIndex:         Int = -1): Constant() {

    var bootstrapMethodAttrIndex: Int = bootstrapMethodAttrIndex
        private set

    var nameAndTypeIndex: Int = nameAndTypeIndex
        private set

    fun getNameAndType(classFile: ClassFile): NameAndTypeConstant {
        return classFile.getNameAndType(nameAndTypeIndex)
    }

    fun getMemberName(classFile: ClassFile): String {
        return getNameAndType(classFile).getMemberName(classFile)
    }

    fun getDescriptor(classFile: ClassFile): String {
        return getNameAndType(classFile).getDescriptor(classFile)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        bootstrapMethodAttrIndex = input.readUnsignedShort()
        nameAndTypeIndex         = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(bootstrapMethodAttrIndex)
        output.writeShort(nameAndTypeIndex)
    }

    fun nameAndTypeConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(nameAndTypeIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitNameAndTypeConstant(classFile, this, PropertyAccessor(::nameAndTypeIndex))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BootstrapRefConstant

        return bootstrapMethodAttrIndex == other.bootstrapMethodAttrIndex &&
               nameAndTypeIndex         == other.nameAndTypeIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(bootstrapMethodAttrIndex, nameAndTypeIndex)
    }
}