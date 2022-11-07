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
package org.tinygears.bat.smali

import org.tinygears.bat.dexfile.ClassDef
import org.tinygears.bat.dexfile.DexFile
import org.tinygears.bat.dexfile.visitor.ClassDefVisitor
import org.tinygears.bat.io.OutputStreamFactory
import org.tinygears.bat.smali.disassemble.SmaliPrinter

class SmaliDisassembler(private val outputStreamFactory: OutputStreamFactory) : ClassDefVisitor {
    override fun visitClassDef(dexFile: DexFile, classDef: ClassDef) {
        outputStreamFactory.createOutputStream(classDef.getClassName(dexFile)).bufferedWriter().use { writer ->
            SmaliPrinter(writer).visitClassDef(dexFile, classDef)
        }
    }
}