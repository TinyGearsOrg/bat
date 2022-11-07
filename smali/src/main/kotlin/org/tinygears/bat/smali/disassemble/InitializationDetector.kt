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
package org.tinygears.bat.smali.disassemble

import org.tinygears.bat.dexfile.ClassDef
import org.tinygears.bat.dexfile.Code
import org.tinygears.bat.dexfile.DexFile
import org.tinygears.bat.dexfile.EncodedMethod
import org.tinygears.bat.dexfile.instruction.DexInstruction
import org.tinygears.bat.dexfile.instruction.FieldInstruction
import org.tinygears.bat.dexfile.instruction.visitor.InstructionVisitor
import org.tinygears.bat.dexfile.util.DexType

internal class InitializationDetector(private val name: String, private val type: DexType) : InstructionVisitor {
    var fieldIsSetInStaticInitializer = false
        private set

    override fun visitAnyInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: DexInstruction) {}

    override fun visitFieldInstruction(dexFile: DexFile, classDef: ClassDef, method: EncodedMethod, code: Code, offset: Int, instruction: FieldInstruction) {
        val mnemonic = instruction.mnemonic

        if (mnemonic.contains("sput")) {
            val fieldID = instruction.getField(dexFile)
            if (fieldID.getName(dexFile) == name && fieldID.getType(dexFile) == type) {
                fieldIsSetInStaticInitializer = true
            }
        }
    }
}