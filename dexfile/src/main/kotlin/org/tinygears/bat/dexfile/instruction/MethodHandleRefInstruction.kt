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
package org.tinygears.bat.dexfile.instruction

import org.tinygears.bat.dexfile.*
import org.tinygears.bat.dexfile.instruction.InstructionFormat.*
import org.tinygears.bat.dexfile.instruction.visitor.InstructionVisitor
import org.tinygears.bat.util.toHexString

class MethodHandleRefInstruction: DexInstruction {

    var methodHandleIndex: Int = NO_INDEX
        internal set

    private constructor(opCode: DexOpCode): super(opCode)

    private constructor(opCode: DexOpCode, methodHandleIndex: Int, vararg registers: Int): super(opCode, *registers) {
        require(methodHandleIndex >= 0) { "methodHandleIndex must not be negative for instruction ${opCode.mnemonic}" }
        this.methodHandleIndex = methodHandleIndex
    }

    fun getMethodHandle(dexFile: DexFile): MethodHandle {
        return dexFile.getMethodHandle(methodHandleIndex)
    }

    override fun read(instructions: ShortArray, offset: Int) {
        super.read(instructions, offset)

        methodHandleIndex = when (opCode.format) {
            FORMAT_21c -> instructions[offset + 1].toInt() and 0xffff

            else -> error("unexpected format '${opCode.format}' for opcode '${opCode.mnemonic}'")
        }
    }

    override fun writeData(): ShortArray {
        val data = super.writeData()

        when (opCode.format) {
            FORMAT_21c -> data[1] = methodHandleIndex.toShort()

            else -> {}
        }
        return data
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {
        visitor.visitMethodHandleRefInstruction(dexFile, classDef, method, code, offset, this)
    }

    override fun toString(): String {
        return super.toString() + ", method_handle@${toHexString(methodHandleIndex, 4)}"
    }

    companion object {
        fun of(opCode: DexOpCode, methodHandleIndex: Int, vararg registers: Int): MethodHandleRefInstruction {
            return MethodHandleRefInstruction(opCode, methodHandleIndex, *registers)
        }

        internal fun create(opCode: DexOpCode): MethodHandleRefInstruction {
            return MethodHandleRefInstruction(opCode)
        }
    }
}