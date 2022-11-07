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
package org.tinygears.bat.dexfile.instruction.editor

import org.tinygears.bat.dexfile.ClassDef
import org.tinygears.bat.dexfile.Code
import org.tinygears.bat.dexfile.DexFile
import org.tinygears.bat.dexfile.EncodedMethod
import org.tinygears.bat.dexfile.instruction.DexInstruction
import org.tinygears.bat.dexfile.instruction.DexOpCode
import org.tinygears.bat.dexfile.instruction.visitor.InstructionVisitor

internal class LabelInstruction private constructor(private val label: String) : DexInstruction(DexOpCode.INTERNAL_LABEL) {

    override fun read(instructions: ShortArray, offset: Int) {}

    override fun write(writer: InstructionWriter, offset: Int, offsetMap: OffsetMap?) {
        if (offsetMap != null) {
            updateOffsets(offset, offsetMap)
        }
    }

    override fun updateOffsets(offset: Int, offsetMap: OffsetMap) {
        offsetMap.setLabel(label, offset)
    }

    override fun writeData(): ShortArray {
        error("should never be called")
    }

    override fun accept(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, visitor: InstructionVisitor) {}

    companion object {
        fun of(label: String): LabelInstruction {
            return LabelInstruction(label)
        }
    }
}