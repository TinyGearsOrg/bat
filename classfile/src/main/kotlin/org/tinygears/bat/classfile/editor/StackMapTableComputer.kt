/*
 *  Copyright (c) 2022 Thomas Neidhart.
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
import org.tinygears.bat.classfile.attribute.preverification.*
import org.tinygears.bat.classfile.constant.editor.ConstantPoolEditor
import org.tinygears.bat.classfile.instruction.JvmInstruction
import org.tinygears.bat.classfile.util.getLoggerFor
import org.tinygears.bat.classfile.evaluation.*
import org.tinygears.bat.util.Logger
import org.tinygears.bat.util.LoggerFactory

/**
 * A [FrameProcessor] to compute the necessary [StackMapFrame] entries for the
 * visited code fragments.
 *
 * TODO: currently there is no liveliness analysis for variables.
 */
internal class StackMapTableComputer constructor(private val classFile: ClassFile,
                                                 private val method:    Method): FrameProcessor {
    private val logger: Logger = LoggerFactory.getLoggerFor(StackMapTableComputer::class.java, classFile, method)

    private val constantPoolEditor = ConstantPoolEditor.of(classFile)
    private val frameList: MutableList<StackMapFrame> = mutableListOf()

    val frames: List<StackMapFrame>
        get() = frameList

    lateinit var lastOffsetFramePair: Pair<Int, Frame>

    init {
        logger.info { "computing stack map table for ${method.getFullExternalMethodSignature(classFile)}" }
    }

    override fun handleInstruction(offset: Int, flags: Int, instruction: JvmInstruction, frameBefore: Frame, frameAfter: Frame) {
        if (offset == 0) {
            lastOffsetFramePair = Pair(offset, frameBefore)
        }

        if (flags and CodeAnalyzer.BRANCH_TARGET     != 0 ||
            flags and CodeAnalyzer.EXCEPTION_HANDLER != 0) {

            val currentFrame = frameBefore

            val (lastOffset, lastFrame) = lastOffsetFramePair
            val offsetDelta = if (frames.isEmpty()) { offset - lastOffset } else { offset - lastOffset - 1 }

            var frame =
                if (currentFrame.stackSize == 0) {
                    if (currentFrame.variables == lastFrame.variables) {
                        sameFrame(offsetDelta)
                    } else if (currentFrame.variableCount > lastFrame.variableCount) {
                        val lastVariables    = lastFrame.variables.toVerificationTypeList(constantPoolEditor)
                        val currentVariables = currentFrame.variables.toVerificationTypeList(constantPoolEditor)
                        appendFrame(offsetDelta, lastVariables, currentVariables)
                    } else if (currentFrame.variableCount < lastFrame.variableCount) {
                        val variables = currentFrame.variables.toVerificationTypeList(constantPoolEditor)
                        chopFrame(offsetDelta, lastFrame.variableSize - currentFrame.variableSize, variables)
                    } else {
                        null
                    }
                } else if (currentFrame.stackSize == 1) {
                    if (currentFrame.variables == lastFrame.variables) {
                        sameLocalsOneStackFrame(offsetDelta, currentFrame.peek().toVerificationType(constantPoolEditor))
                    } else {
                        null
                    }
                } else {
                    null
                }

            if (frame == null) {
                val currentVariables = currentFrame.variables.toVerificationTypeList(constantPoolEditor)
                val currentStack     = currentFrame.stack.toVerificationTypeList(constantPoolEditor)
                frame = fullFrame(offsetDelta, currentVariables, currentStack)
            }

            logger.trace { "offset $offset: adding frame $frame"}

            frameList.add(frame)

            lastOffsetFramePair = Pair(offset, currentFrame)
        }
    }
}
