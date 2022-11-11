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
package org.tinygears.bat.classfile.editor

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.Method
import org.tinygears.bat.classfile.instruction.JvmInstruction
import org.tinygears.bat.classfile.util.getLoggerFor
import org.tinygears.bat.util.Logger
import org.tinygears.bat.util.LoggerFactory
import org.tinygears.bat.classfile.verifier.CodeAnalyzer
import org.tinygears.bat.classfile.verifier.Frame
import org.tinygears.bat.classfile.verifier.FrameProcessor

internal class StackMapTableComputer constructor(val classFile: ClassFile, val method: Method): FrameProcessor {
    private val logger: Logger = LoggerFactory.getLoggerFor(StackMapTableComputer::class.java, classFile, method)

    init {
        logger.info { "computing stack map table for ${method.getFullExternalMethodSignature(classFile)}" }
    }

    override fun handleInstruction(offset: Int, flags: Int, instruction: JvmInstruction, frameBefore: Frame, frameAfter: Frame) {
        if (flags and CodeAnalyzer.BRANCH_TARGET != 0) {
            logger.trace { "found branch target at offset $offset" }
        }
    }
}
