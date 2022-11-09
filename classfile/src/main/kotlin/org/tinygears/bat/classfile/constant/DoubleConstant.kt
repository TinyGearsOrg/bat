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
 * A constant representing a CONSTANT_Double_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.5">CONSTANT_Double_info Structure</a>
 */
class DoubleConstant private constructor(value: Double = 0.0) : Constant() {

    override val type: ConstantType
        get() = ConstantType.DOUBLE

    var value: Double = value
        private set

    fun copyWith(value: Double): DoubleConstant {
        return DoubleConstant(value)
    }
    
    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        val highBytes = input.readUnsignedInt()
        val lowBytes  = input.readUnsignedInt()
        val bits = (highBytes shl 32) + lowBytes
        value = Double.fromBits(bits)
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        val bits = value.toBits()
        val highBytes = (bits shr 32).toInt()
        val lowBytes  = bits.toInt()
        output.writeInt(highBytes)
        output.writeInt(lowBytes)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitDoubleConstant(classFile, index, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleConstant) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }

    override fun toString(): String {
        return "DoubleConstant[$value]"
    }

    companion object {
        internal fun empty(): DoubleConstant {
            return DoubleConstant()
        }

        fun of(value: Double): DoubleConstant {
            return DoubleConstant(value)
        }
    }
}