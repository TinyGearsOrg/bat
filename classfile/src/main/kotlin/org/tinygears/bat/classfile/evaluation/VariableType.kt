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
package org.tinygears.bat.classfile.evaluation

import org.tinygears.bat.classfile.attribute.preverification.*
import org.tinygears.bat.classfile.constant.editor.ConstantPoolEditor
import org.tinygears.bat.util.*
import java.util.*

abstract class VariableType {
    abstract val name: String

    abstract val isCategory2: Boolean
    abstract val operandSize: Int

    abstract fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    override fun toString(): String {
        return name
    }

    companion object {
        fun of(jvmType: JvmType): VariableType {
            return if (jvmType.isReferenceType) {
                JavaReferenceType.of(jvmType)
            } else if (jvmType.isPrimitiveType) {
                when (jvmType.type) {
                    BYTE_TYPE,
                    SHORT_TYPE,
                    CHAR_TYPE,
                    BOOLEAN_TYPE,
                    INT_TYPE    -> IntegerType
                    FLOAT_TYPE  -> FloatType
                    LONG_TYPE   -> LongType
                    DOUBLE_TYPE -> DoubleType
                    else        -> error("unexpected primitive type '$jvmType'")
                }
            } else {
                error("unexpected type '$jvmType'")
            }
        }
    }
}

object TopType: VariableType() {
    override val name: String
        get() = "top"

    override val isCategory2: Boolean
        get() = false

    override val operandSize: Int
        get() = 1

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return TopVariable
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

object IntegerType: VariableType() {
    override val name: String
        get() = "int"

    override val isCategory2: Boolean
        get() = false

    override val operandSize: Int
        get() = 1

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return IntegerVariable
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

object FloatType: VariableType() {
    override val name: String
        get() = "float"

    override val isCategory2: Boolean
        get() = false

    override val operandSize: Int
        get() = 1

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return FloatVariable
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

object LongType: VariableType() {
    override val name: String
        get() = "long"

    override val isCategory2: Boolean
        get() = true

    override val operandSize: Int
        get() = 2

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return LongVariable
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

object DoubleType: VariableType() {
    override val name: String
        get() = "double"

    override val isCategory2: Boolean
        get() = true

    override val operandSize: Int
        get() = 2

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return DoubleVariable
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

abstract class ReferenceType constructor(protected val _classType: JvmType?): VariableType() {
    override val isCategory2: Boolean
        get() = false

    override val operandSize: Int
        get() = 1
}

class JavaReferenceType private constructor(classType: JvmType): ReferenceType(classType) {
    override val name: String
        get() = "class($classType)"

    val classType: JvmType
        get() = _classType!!

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return ObjectVariable.of(constantPoolEditor.addOrGetClassConstantIndex(classType.toJvmClassName().toString()))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JavaReferenceType
        return classType == other.classType
    }

    override fun hashCode(): Int {
        return classType.hashCode()
    }

    companion object {
        fun of(classType: JvmType): JavaReferenceType {
            return JavaReferenceType(classType)
        }
    }
}

class UninitializedThisType private constructor(classType: JvmType): ReferenceType(classType) {
    override val name: String
        get() = "uninitializedThis($classType)"

    val classType: JvmType
        get() = _classType!!

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return UninitializedThisVariable
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JavaReferenceType
        return classType == other.classType
    }

    override fun hashCode(): Int {
        return classType.hashCode()
    }

    companion object {
        fun of(classType: JvmType): UninitializedThisType {
            return UninitializedThisType(classType)
        }
    }
}

class UninitializedType private constructor(classType: JvmType, val offset: Int): ReferenceType(classType) {
    override val name: String
        get() = "uninitialized($classType, $offset)"

    val classType: JvmType
        get() = _classType!!

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return UninitializedVariable.of(offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UninitializedType

        return offset    == other.offset &&
               classType == other.classType
    }

    override fun hashCode(): Int {
        return Objects.hash(offset, classType)
    }

    companion object {
        fun of(classType: JvmType, offset: Int): UninitializedType {
            return UninitializedType(classType, offset)
        }
    }
}

object NullReference: ReferenceType(null) {
    override val name: String
        get() = "nullReference"

    override fun toVerificationType(constantPoolEditor: ConstantPoolEditor): VerificationType {
        return NullVariable
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}