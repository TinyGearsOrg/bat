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
 * A constant representing a CONSTANT_Integer_info structure in a class file.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.4.4">CONSTANT_Integer_info Structure</a>
 */
class IntegerConstant private constructor(value: Int = 0) : Constant() {

    override val type: ConstantType
        get() = ConstantType.INTEGER

    var value: Int = value
        private set

    fun copyWith(value: Int): IntegerConstant {
        return IntegerConstant(value)
    }

    @Throws(IOException::class)
    override fun readConstantInfo(input: ClassDataInput) {
        value = input.readInt()
    }

    @Throws(IOException::class)
    override fun writeConstantInfo(output: ClassDataOutput) {
        output.writeInt(value)
    }

    override fun accept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        visitor.visitIntegerConstant(classFile, index, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntegerConstant) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }

    override fun toString(): String {
        return "IntegerConstant[$value]"
    }

    companion object {
        internal fun empty(): IntegerConstant {
            return IntegerConstant()
        }

        fun of(value: Int): IntegerConstant {
            return IntegerConstant(value)
        }
    }
}