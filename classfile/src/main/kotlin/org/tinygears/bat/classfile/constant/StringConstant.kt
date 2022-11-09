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
 * A constant representing a CONSTANT_String_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.3">CONSTANT_String_info Structure</a>
 */
class StringConstant private constructor(stringIndex: Int = -1): Constant() {

    override val type: ConstantType
        get() = ConstantType.STRING

    var stringIndex: Int = stringIndex
        private set

    fun getString(classFile: ClassFile): String {
        return classFile.getString(stringIndex)
    }

    fun copyWith(stringIndex: Int): StringConstant {
        return StringConstant(stringIndex)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        stringIndex = input.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeShort(stringIndex)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitStringConstant(classFile, index, this);
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::stringIndex))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringConstant) return false

        return stringIndex == other.stringIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(stringIndex)
    }

    override fun toString(): String {
        return "StringConstant[#$stringIndex]"
    }

    companion object {
        internal fun empty(): StringConstant {
            return StringConstant()
        }

        fun of(stringIndex: Int): StringConstant {
            require(stringIndex >= 1) { "stringIndex must be a positive number" }
            return StringConstant(stringIndex)
        }
    }
}