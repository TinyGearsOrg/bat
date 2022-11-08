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
package org.tinygears.bat.jasm.disassemble

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.attribute.annotation.*
import org.tinygears.bat.classfile.attribute.annotation.visitor.TargetInfoVisitor
import org.tinygears.bat.io.IndentingPrinter
import java.util.*

internal class TargetInfoPrinter constructor(private val printer: IndentingPrinter): TargetInfoVisitor {

    override fun visitAnyTargetInfo(classFile: ClassFile, targetInfo: TargetInfo) {
        TODO("need to implement target info type ${targetInfo.type}")
    }

    override fun visitCatchTargetInfo(classFile: ClassFile, targetInfo: CatchTargetInfo) {
        printer.print("${targetInfo.typeAsString}(exception=${targetInfo.exceptionTableIndex})")
    }

    override fun visitTypeParameterTargetInfo(classFile: ClassFile, targetInfo: TypeParameterTargetInfo) {
        printer.print("${targetInfo.type}(type=${targetInfo.typeParameterIndex})")
    }

    override fun visitThrowsTargetInfo(classFile: ClassFile, targetInfo: ThrowsTargetInfo) {
        printer.print("${targetInfo.typeAsString}(type=${targetInfo.throwsTypeIndex})")
    }

    override fun visitOffsetTargetInfo(classFile: ClassFile, targetInfo: OffsetTargetInfo) {
        printer.print("${targetInfo.typeAsString}(offset=${targetInfo.offset})")
    }

    override fun visitLocalVarTargetInfo(classFile: ClassFile, targetInfo: LocalVarTargetInfo) {
        val localVarInfo = targetInfo.joinToString(separator = ",", transform = {
            "{start_pc=${it.startPC}, length=${it.length},index=${it.variableIndex}}"
        })
        printer.print(", $localVarInfo")
    }

    override fun visitTypeArgumentTargetInfo(classFile: ClassFile, targetInfo: TypeArgumentTargetInfo) {
        printer.print("${targetInfo.typeAsString}(offset=${targetInfo.offset},type=${targetInfo.typeArgumentIndex})")
    }

    override fun visitSuperTypeTargetInfo(classFile: ClassFile, targetInfo: SuperTypeTargetInfo) {
        printer.print("${targetInfo.typeAsString}(type=${targetInfo.superTypeIndex})")
    }

    override fun visitTypeParameterBoundTargetInfo(classFile: ClassFile, targetInfo: TypeParameterBoundTargetInfo) {
        printer.print("${targetInfo.typeAsString}(param=${targetInfo.typeParameterIndex},bound=${targetInfo.boundIndex})")
    }

    override fun visitFormalParameterTargetInfo(classFile: ClassFile, targetInfo: FormalParameterTargetInfo) {
        printer.print("${targetInfo.typeAsString}(param=${targetInfo.formalParameterIndex})")
    }

    override fun visitEmptyTargetInfo(classFile: ClassFile, targetInfo: EmptyTargetInfo) {
        printer.print(targetInfo.typeAsString)
    }
}

private val TargetInfo.typeAsString: String
    get() = this.type.toString().lowercase(Locale.getDefault())