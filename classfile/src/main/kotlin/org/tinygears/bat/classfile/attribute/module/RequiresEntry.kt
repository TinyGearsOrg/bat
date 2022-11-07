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

package org.tinygears.bat.classfile.attribute.module

import org.tinygears.bat.classfile.AccessFlag
import org.tinygears.bat.classfile.AccessFlagTarget.*
import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.accessFlagsToSet
import org.tinygears.bat.classfile.constant.visitor.PropertyAccessor
import org.tinygears.bat.classfile.constant.visitor.ReferencedConstantVisitor
import org.tinygears.bat.classfile.io.ClassDataInput
import org.tinygears.bat.classfile.io.ClassDataOutput
import org.tinygears.bat.classfile.io.ClassFileContent

data class RequiresEntry
    private constructor(private var _requiredModuleIndex:  Int = -1,
                        private var _flags:                Int =  0,
                        private var _requiredVersionIndex: Int = -1): ClassFileContent() {

    val requiredModuleIndex: Int
        get() = _requiredModuleIndex

    val flags: Int
        get() = _flags

    val flagsAsSet: Set<AccessFlag>
        get() = accessFlagsToSet(flags, REQUIRED_MODULE)

    val requiredVersionIndex: Int
        get() = _requiredVersionIndex

    override val contentSize: Int
        get() = 6

    fun getRequiredModuleName(classFile: ClassFile): String {
        return classFile.getModule(requiredModuleIndex).getModuleName(classFile)
    }

    fun getRequiredVersion(classFile: ClassFile): String? {
        return classFile.getStringOrNull(requiredVersionIndex)
    }

    private fun read(input: ClassDataInput) {
        _requiredModuleIndex  = input.readUnsignedShort()
        _flags                = input.readUnsignedShort()
        _requiredVersionIndex = input.readUnsignedShort()
    }

    override fun write(output: ClassDataOutput) {
        output.writeShort(_requiredModuleIndex)
        output.writeShort(_flags)
        output.writeShort(_requiredVersionIndex)
    }

    fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        visitor.visitModuleConstant(classFile, this, PropertyAccessor(::_requiredModuleIndex))
        if (_requiredVersionIndex > 0) {
            visitor.visitUtf8Constant(classFile, this, PropertyAccessor(::_requiredVersionIndex))
        }
    }

    companion object {
        internal fun read(input: ClassDataInput): RequiresEntry {
            val entry = RequiresEntry()
            entry.read(input)
            return entry
        }
    }
}