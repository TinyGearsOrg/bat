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

import org.tinygears.bat.classfile.attribute.preverification.VerificationType
import org.tinygears.bat.classfile.constant.editor.ConstantPoolEditor
import org.tinygears.bat.util.mutableListOfCapacity
import java.util.*

class Frame private constructor(private val _variables: MutableList<VariableType> = mutableListOfCapacity(0),
                                private val _stack:     MutableList<VariableType> = mutableListOfCapacity(0)) {

    val variables: List<VariableType>
        get() = _variables

    val stack: List<VariableType>
        get() = _stack

    val stackSize: Int
        get() = stack.fold(0) { agg, type -> agg + type.operandSize }

    val variableSize: Int
        get() = variables.size

    val variableCount: Int
        get() {
            var i = 0
            var count = 0
            while (i < variables.size) {
                val type = variables[i]
                count++
                i += type.operandSize
            }
            return count
        }

    fun copy(): Frame {
        return Frame(variables.toMutableList(), stack.toMutableList())
    }

    fun pop(): VariableType {
        return _stack.removeLast()
    }

    fun peek(): VariableType {
        return _stack.last()
    }

    fun pop(count: Int) {
        for (i in 0 until count) {
            _stack.removeLast()
        }
    }

    fun push(type: VariableType) {
        _stack.add(type)
    }

    fun clearStack() {
        _stack.clear()
    }

    fun load(variable: Int): VariableType {
        val value = variables[variable]
        check(value !is TopType)
        return value
    }

    fun store(variable: Int, type: VariableType) {
        val maxVariableIndex = if (type.isCategory2) variable + 1 else variable

        ensureVariableCapacity(maxVariableIndex)
        _variables[variable] = type
    }

    private fun ensureVariableCapacity(capacity: Int) {
        while (capacity >= variables.size) {
            _variables.add(TopType)
        }
    }

    internal fun referenceInitialized(reference: VariableType) {
        require(reference is UninitializedType ||
                reference is UninitializedThisType)

        if (reference is UninitializedType) {
            val initializedReference = JavaReferenceType.of(reference.classType)

            for (i in _variables.indices) {
                if (_variables[i] == reference) {
                    _variables[i] = initializedReference
                }
            }

            for (i in _stack.indices) {
                if (_stack[i] == reference) {
                    _stack[i] = initializedReference
                }
            }
        } else if (reference is UninitializedThisType) {
            val initializedReference = JavaReferenceType.of(reference.classType)

            for (i in _variables.indices) {
                if (_variables[i] is UninitializedThisType) {
                    _variables[i] = initializedReference
                }
            }

            for (i in _stack.indices) {
                if (_stack[i] is UninitializedThisType) {
                    _stack[i] = initializedReference
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Frame

        return _variables == other._variables &&
               _stack     == other._stack
    }

    override fun hashCode(): Int {
        return Objects.hash(_variables, _stack)
    }

    override fun toString(): String {
        return buildString {
            append(_variables.joinToString(separator = ", ", prefix = "{", postfix = "}", transform = { it?.name ?: "" }))
            append(" ")
            append(_stack.joinToString(separator = ", ", prefix = "{", postfix = "}", transform = { it.name }))
        }
    }

    companion object {
        fun empty(): Frame {
            return Frame()
        }
    }
}

fun List<VariableType>.toVerificationTypeList(constantPoolEditor: ConstantPoolEditor): List<VerificationType> {
    val result = mutableListOf<VerificationType>()
    var i = 0
    while (i < size) {
        val type = this[i]
        result.add(type.toVerificationType(constantPoolEditor))
        i += type.operandSize
    }
    return result
}