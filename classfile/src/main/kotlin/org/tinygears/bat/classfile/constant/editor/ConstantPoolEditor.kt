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
package org.tinygears.bat.classfile.constant.editor

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.constant.*
import org.tinygears.bat.classfile.constant.ConstantPool

class ConstantPoolEditor private constructor(private val constantPool: ConstantPool) {

    fun replaceConstant(index: Int, newConstant: Constant) {
        constantPool[index] = newConstant
    }

    fun addOrGetUtf8ConstantIndex(string: String): Int {
        val constant = Utf8Constant.of(string)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetIntegerConstantIndex(value: Int): Int {
        val constant = IntegerConstant.of(value)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetLongConstantIndex(value: Long): Int {
        val constant = LongConstant.of(value)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetFloatConstantIndex(value: Float): Int {
        val constant = FloatConstant.of(value)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetDoubleConstantIndex(value: Double): Int {
        val constant = DoubleConstant.of(value)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetClassConstantIndex(className: String): Int {
        val nameIndex = addOrGetUtf8ConstantIndex(className)
        val constant  = ClassConstant.of(nameIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetNameAndTypeConstantIndex(name: String, descriptor: String): Int {
        val nameIndex       = addOrGetUtf8ConstantIndex(name)
        val descriptorIndex = addOrGetUtf8ConstantIndex(descriptor)
        val constant        = NameAndTypeConstant.of(nameIndex, descriptorIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetFieldRefConstantIndex(className: String, fieldName: String, descriptor: String): Int {
        val classIndex       = addOrGetClassConstantIndex(className)
        val nameAndTypeIndex = addOrGetNameAndTypeConstantIndex(fieldName, descriptor)
        val constant         = FieldrefConstant.of(classIndex, nameAndTypeIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetMethodRefConstantIndex(className: String, methodName: String, descriptor: String): Int {
        val classIndex       = addOrGetClassConstantIndex(className)
        val nameAndTypeIndex = addOrGetNameAndTypeConstantIndex(methodName, descriptor)
        val constant         = MethodrefConstant.of(classIndex, nameAndTypeIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetInterfaceMethodRefConstantIndex(className: String, methodName: String, descriptor: String): Int {
        val classIndex       = addOrGetClassConstantIndex(className)
        val nameAndTypeIndex = addOrGetNameAndTypeConstantIndex(methodName, descriptor)
        val constant         = InterfaceMethodrefConstant.of(classIndex, nameAndTypeIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetInvokeDynamicConstantIndex(bootstrapMethodAttrIndex: Int, methodName: String, descriptor: String): Int {
        val nameAndTypeIndex = addOrGetNameAndTypeConstantIndex(methodName, descriptor)
        val constant         = InvokeDynamicConstant.of(bootstrapMethodAttrIndex, nameAndTypeIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetMethodTypeConstantIndex(descriptor: String): Int {
        val descriptorIndex = addOrGetUtf8ConstantIndex(descriptor)
        val constant        = MethodTypeConstant.of(descriptorIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    fun addOrGetMethodHandleConstantIndex(referenceKind: ReferenceKind, className: String, methodName: String, descriptor: String): Int {
        val methodRefIndex = addOrGetMethodRefConstantIndex(className, methodName, descriptor)
        val constant       = MethodHandleConstant.of(referenceKind, methodRefIndex)
        val index = constantPool.getConstantIndex(constant)
        return if (index == -1) {
            constantPool.addConstant(constant)
        } else {
            index
        }
    }

    companion object {
        fun of(classFile: ClassFile): ConstantPoolEditor {
            return ConstantPoolEditor(classFile.constantPool)
        }
    }
}