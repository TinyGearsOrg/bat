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

import org.tinygears.bat.classfile.*
import org.tinygears.bat.classfile.constant.ReferenceKind.*
import org.tinygears.bat.classfile.constant.visitor.ConstantVisitor
import org.tinygears.bat.classfile.constant.visitor.PropertyAccessor
import org.tinygears.bat.classfile.constant.visitor.ReferencedConstantVisitor
import org.tinygears.bat.classfile.io.ClassDataInput
import org.tinygears.bat.classfile.io.ClassDataOutput
import java.io.IOException
import java.util.*

/**
 * A constant representing a CONSTANT_MethodHandle_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.8">CONSTANT_MethodHandle_info Structure</a>
 */
class MethodHandleConstant private constructor(referenceKind:  ReferenceKind = GET_FIELD,
                                               referenceIndex: Int           = -1) : Constant() {

    override val type: ConstantType
        get() = ConstantType.METHOD_HANDLE

    var referenceKind: ReferenceKind = referenceKind
        private set

    var referenceIndex: Int = referenceIndex
        private set

    fun getReferencedFieldOrMethod(classFile: ClassFile): RefConstant {
        return classFile.getRefConstant(referenceIndex)
    }

    fun copyWith(referenceKind: ReferenceKind = this.referenceKind, referenceIndex: Int = this.referenceIndex): MethodHandleConstant {
        return MethodHandleConstant(referenceKind, referenceIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        referenceKind  = ReferenceKind.of(input.readUnsignedByte())
        referenceIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeByte(referenceKind.value)
        output.writeShort(referenceIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitMethodHandleConstant(classFile, index, this)
    }

    fun referenceAccept(classFile: ClassFile, visitor: ConstantVisitor) {
        classFile.constantAccept(referenceIndex, visitor)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        val propertyAccessor = PropertyAccessor(::referenceIndex)

        when (referenceKind) {
            GET_FIELD,
            GET_STATIC,
            PUT_FIELD,
            PUT_STATIC -> visitor.visitFieldRefConstant(classFile, this, propertyAccessor)

            INVOKE_VIRTUAL,
            NEW_INVOKE_SPECIAL -> visitor.visitMethodRefConstant(classFile, this, propertyAccessor)

            INVOKE_STATIC,
            INVOKE_SPECIAL -> {
                val constant = classFile.getConstant(referenceIndex)
                when (constant.type) {
                    ConstantType.METHOD_REF           -> visitor.visitMethodRefConstant(classFile, this, propertyAccessor)
                    ConstantType.INTERFACE_METHOD_REF -> visitor.visitInterfaceMethodRefConstant(classFile, this, propertyAccessor)
                    else                              -> error("unexpected referenced constant '${constant}'")
                }
            }

            INVOKE_INTERFACE -> visitor.visitInterfaceMethodRefConstant(classFile, this, propertyAccessor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MethodHandleConstant) return false

        return referenceKind  == other.referenceKind &&
               referenceIndex == other.referenceIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(referenceKind, referenceIndex)
    }

    override fun toString(): String {
        return "MethodHandleConstant[$referenceKind,#$referenceIndex]"
    }

    companion object {
        internal fun empty(): MethodHandleConstant {
            return MethodHandleConstant()
        }

        fun of(referenceKind: ReferenceKind, referenceIndex: Int): MethodHandleConstant {
            require(referenceIndex >= 1) { "referenceIndex must be a positive number" }
            return MethodHandleConstant(referenceKind, referenceIndex)
        }
    }
}

enum class ReferenceKind constructor(val value: Int, val simpleName: String) {
    GET_FIELD         (REF_getField,         "REF_getField"),
    GET_STATIC        (REF_getStatic,        "REF_getStatic"),
    PUT_FIELD         (REF_putField,         "REF_putField"),
    PUT_STATIC        (REF_putStatic,        "REF_putStatic"),
    INVOKE_VIRTUAL    (REF_invokeVirtual,    "REF_invokeVirtual"),
    INVOKE_STATIC     (REF_invokeStatic,     "REF_invokeStatic"),
    INVOKE_SPECIAL    (REF_invokeSpecial,    "REF_invokeSpecial"),
    NEW_INVOKE_SPECIAL(REF_newInvokeSpecial, "REF_newInvokeSpecial"),
    INVOKE_INTERFACE  (REF_invokeInterface,  "REF_invokeInterface");

    companion object {
        private val valueToReferenceKindMap: Map<Int, ReferenceKind> by lazy {
            values().associateBy { it.value }
        }

        fun of(value: Int): ReferenceKind {
            return valueToReferenceKindMap[value] ?: throw IllegalArgumentException("unknown reference kind '$value'")
        }

        fun ofSimpleName(text: String): ReferenceKind {
            for (refKind in values()) {
                if (refKind.simpleName == text) {
                    return refKind
                }
            }
            error("unexpected refKind '$text'")
        }
    }
}