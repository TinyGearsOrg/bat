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
import org.tinygears.bat.classfile.io.ClassDataInput
import org.tinygears.bat.classfile.io.ClassDataOutput
import java.io.IOException
import java.util.*

/**
 * A constant representing a CONSTANT_Long_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.5">CONSTANT_Long_info Structure</a>
 */
class LongConstant private constructor(value: Long = 0) : Constant() {

    override val type: ConstantType
        get() = ConstantType.LONG

    var value: Long = value
        private set

    fun copyWith(value: Long): LongConstant {
        return LongConstant(value)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        val highBytes = input.readUnsignedInt()
        val lowBytes  = input.readUnsignedInt()
        value = (highBytes shl 32) + lowBytes
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        val highBytes = (value shr 32).toInt()
        val lowBytes  = value.toInt()
        output.writeInt(highBytes)
        output.writeInt(lowBytes)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitLongConstant(classFile, index, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongConstant) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }

    override fun toString(): String {
        return "LongConstant[$value]"
    }

    companion object {
        internal fun empty() : LongConstant {
            return LongConstant()
        }

        fun of(value: Long) : LongConstant {
            return LongConstant(value)
        }
    }
}