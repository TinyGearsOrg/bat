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
import org.tinygears.bat.classfile.evaluation.value.ReferenceValue
import org.tinygears.bat.classfile.evaluation.value.TopValue
import org.tinygears.bat.classfile.evaluation.value.UninitializedReferenceValue
import org.tinygears.bat.classfile.evaluation.value.Value
import org.tinygears.bat.util.mutableListOfCapacity
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class Frame private constructor(private val _variables: MutableList<Value>         = mutableListOfCapacity(0),
                                private val _stack:     MutableList<Value>         = mutableListOfCapacity(0),
                                private val _alive:     MutableList<AtomicBoolean> = mutableListOfCapacity(0)) {

    val variables: List<Value>
        get() = _variables

    val stack: List<Value>
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

    val aliveVariableCount: Int
        get() {
            var i = 0
            var count = 0
            while (i < variables.size) {
                val type = variables[i]
                if (_alive[i].get()) {
                    count++
                }
                i += type.operandSize
            }
            return count
        }

    fun copy(): Frame {
        return Frame(variables.toMutableList(), stack.toMutableList(), _alive.toMutableList())
    }

    fun pop(): Value {
        return _stack.removeLast()
    }

    fun peek(): Value {
        return _stack.last()
    }

    fun pop(count: Int) {
        for (i in 0 until count) {
            _stack.removeLast()
        }
    }

    fun push(value: Value) {
        _stack.add(value)
    }

    fun clearStack() {
        _stack.clear()
    }

    fun isAlive(variable: Int): Boolean {
        return _alive[variable].get()
    }

    fun load(variable: Int): Value {
        val value = variables[variable]
        check(value !is TopValue) { "trying to load a TopValue at variableIndex $variable" }
        return value
    }

    fun store(variable: Int, value: Value) {
        val maxVariableIndex = if (value.isCategory2) variable + 1 else variable

        ensureVariableCapacity(maxVariableIndex)
        _variables[variable] = value
    }

    internal fun variableRead(variable: Int) {
        _alive[variable].set(true)
    }

    internal fun resetVariableLiveness(variable: Int) {
        _alive[variable] = AtomicBoolean(false)
    }

    internal fun resetVariableLiveness() {
        for (i in _alive.indices) {
            _alive[i] = AtomicBoolean(_alive[i].get())
        }
    }

    internal fun mergeLiveness(other: Frame) {
        for (i in _alive.indices) {
            if (i in other._alive.indices) {
                _alive[i].set(_alive[i].get() or other._alive[i].get())
            }
        }
    }

    internal fun variableWritten(variable: Int) {
        val maxVariableIndex = if (_variables[variable].isCategory2) variable + 1 else variable

        ensureAliveCapacity(maxVariableIndex)
        resetVariableLiveness(variable)
    }

    private fun ensureVariableCapacity(capacity: Int) {
        while (capacity >= variables.size) {
            _variables.add(TopValue)
        }
    }

    private fun ensureAliveCapacity(capacity: Int) {
        while (capacity >= _alive.size) {
            _alive.add(AtomicBoolean(false))
        }
    }

    internal fun referenceInitialized(reference: Value, initializedReference: Value) {
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
            append(_variables.joinToString(separator = ", ", prefix = "{", postfix = "}"))
            append(" ")
            append(_stack.joinToString(separator = ", ", prefix = "{", postfix = "}"))
            append(" ")
            append(_alive.joinToString(separator = ", ", prefix = "{", postfix = "}"))
        }
    }

    companion object {
        fun empty(): Frame {
            return Frame()
        }
    }
}

fun List<Value>.toVerificationTypeList(constantPoolEditor: ConstantPoolEditor): List<VerificationType> {
    val result = mutableListOf<VerificationType>()
    var i = 0
    while (i < size) {
        val value = this[i]
        result.add(value.toVerificationType(constantPoolEditor))
        i += value.operandSize
    }
    return result
}