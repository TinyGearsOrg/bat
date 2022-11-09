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
import org.tinygears.bat.classfile.constant.visitor.ReferencedConstantVisitor
import org.tinygears.bat.classfile.io.ClassDataInput
import org.tinygears.bat.classfile.io.ClassDataOutput
import org.tinygears.bat.util.mutableListOfCapacity
import java.io.IOException

internal class ConstantPool
    private constructor(private var constants: MutableList<Constant?>    = mutableListOfCapacity(1),
                        private var map:       MutableMap<Constant, Int> = mutableMapOf()) {

    init {
        if (constants.isEmpty()) {
            constants.add(null)
        }
        
        constants.filterNotNull().forEachIndexed { index, constant -> map[constant] = index }
    }

    internal val size: Int
        get() = constants.size

    operator fun get(index: Int): Constant {
        require(constants[index] != null) { "trying to retrieve a null constant at index '$index'" }
        return constants[index]!!
    }

    internal operator fun set(index: Int, constant: Constant) {
        val oldConstant = constants[index]

        if (oldConstant != null) {
            require(oldConstant.javaClass == constant.javaClass) { "replacing constant of different type $constant != $oldConstant" }
            map.remove(oldConstant)
        }

        constants[index] = constant
        map[constant]    = index
    }

    internal fun addConstant(constant: Constant): Int {
        constants.add(constant)
        val constantIndex = constants.lastIndex

        if (constant.constantPoolSize > 1) {
            constants.add(null)
        }

        map[constant] = constantIndex

        return constantIndex
    }

    internal fun getConstantIndex(constant: Constant): Int {
        return map[constant] ?: -1
    }

    @Throws(IOException::class)
    internal fun read(input: ClassDataInput) {
        val entries = input.readUnsignedShort()
        constants = mutableListOfCapacity(entries)
        constants.add(null)
        var i = 1
        while (i < entries) {
            val constant = input.readConstant()
            constants.add(constant)
            if (constant.constantPoolSize > 1) {
                constants.add(null)
                i += 2
            } else {
                i++
            }
        }
    }

    @Throws(IOException::class)
    internal fun write(output: ClassDataOutput) {
        val entries = constants.size
        output.writeShort(entries)
        val it: ListIterator<Constant?> = constants.listIterator(1)
        while (it.hasNext()) {
            val constant = it.next()
            constant?.let {
                output.writeByte(it.type.tag)
                it.writeConstantInfo(output)
            }
        }
    }

    fun accept(classFile: ClassFile, visitor: ConstantVisitor) {
        constants.forEachIndexed { index, constant -> constant?.accept(classFile, index, visitor) }
    }

    fun constantAccept(classFile: ClassFile, index: Int, visitor: ConstantVisitor) {
        if (index in 1 until constants.size) {
            check(constants[index] != null) { "trying to accept a null constant at index $index" }
            constants[index]?.accept(classFile, index, visitor)
        }
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        constants.forEach { constant -> constant?.referencedConstantsAccept(classFile, visitor) }
    }

    override fun toString(): String {
        return "ConstantPool[$size constants]"
    }

    companion object {
        fun empty(): ConstantPool {
            return ConstantPool()
        }
    }
}