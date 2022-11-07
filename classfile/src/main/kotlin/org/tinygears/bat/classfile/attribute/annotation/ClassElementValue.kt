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

package org.tinygears.bat.classfile.attribute.annotation

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.attribute.annotation.visitor.ElementValueVisitor
import org.tinygears.bat.classfile.constant.visitor.ConstantVisitor
import org.tinygears.bat.classfile.constant.visitor.PropertyAccessor
import org.tinygears.bat.classfile.constant.visitor.ReferencedConstantVisitor
import org.tinygears.bat.classfile.io.ClassDataInput
import org.tinygears.bat.classfile.io.ClassDataOutput
import org.tinygears.bat.util.JvmType
import org.tinygears.bat.util.asJvmType
import java.io.IOException

data class ClassElementValue private constructor(private var _classIndex: Int = -1) : ElementValue() {

    override val type: ElementValueType
        get() = ElementValueType.CLASS

    override val contentSize: Int
        get() = 3

    val classIndex: Int
        get() = _classIndex

    fun getType(classFile: ClassFile): JvmType {
        return classFile.getString(classIndex).asJvmType()
    }

    @Throws(IOException::class)
    override fun readElementValue(input: ClassDataInput) {
        _classIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeElementValue(output: ClassDataOutput) {
        output.writeShort(classIndex)
    }

    override fun accept(classFile: ClassFile, visitor: ElementValueVisitor) {
        visitor.visitClassElementValue(classFile, this)
    }

    fun classTypeConstantAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(classIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitClassConstant(classFile, this, PropertyAccessor(::_classIndex))
    }

    companion object {
        internal fun empty(): ClassElementValue {
            return ClassElementValue()
        }
    }
}