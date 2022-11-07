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

/**
 * An abstract base class for constants referencing a bootstrap method,
 * i.e. Dynamic or InvokeDynamic.
 */
abstract class BootstrapRefConstant(protected open var _bootstrapMethodAttrIndex: Int = -1,
                                    protected open var _nameAndTypeIndex:         Int = -1): Constant() {

    val bootstrapMethodAttrIndex: Int
        get() = _bootstrapMethodAttrIndex

    val nameAndTypeIndex: Int
        get() = _nameAndTypeIndex

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
        _bootstrapMethodAttrIndex = input.readUnsignedShort()
        _nameAndTypeIndex         = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(_bootstrapMethodAttrIndex)
        output.writeShort(_nameAndTypeIndex)
    }

    fun nameAndTypeConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(nameAndTypeIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitNameAndTypeConstant(classFile, this, PropertyAccessor(::_nameAndTypeIndex))
    }
}