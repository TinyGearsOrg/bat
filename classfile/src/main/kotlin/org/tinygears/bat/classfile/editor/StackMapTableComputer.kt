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
import org.tinygears.bat.classfile.evaluation.value.Value
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

            logger.debug(currentFrame.toString())

            val (lastOffset, lastFrame) = lastOffsetFramePair
            val offsetDelta = if (frames.isEmpty()) { offset - lastOffset } else { offset - lastOffset - 1 }

            val lastVariables    = trimNonAliveValues(lastFrame)
            val currentVariables = trimNonAliveValues(currentFrame)

            var frame =
                if (currentFrame.stackSize == 0) {
                    if (currentVariables == lastVariables) {
                        sameFrame(offsetDelta)
                    } else if (currentVariables.size > lastVariables.size) {
                        appendFrame(offsetDelta,
                                    lastVariables.toVerificationTypeList(constantPoolEditor),
                                    currentVariables.toVerificationTypeList(constantPoolEditor))
                    } else if (currentFrame.aliveVariableCount < lastFrame.aliveVariableCount) {
                        val variables = currentVariables.toVerificationTypeList(constantPoolEditor)
                        chopFrame(offsetDelta, lastVariables.size - currentVariables.size, variables)
                    } else {
                        null
                    }
                } else if (currentFrame.stackSize == 1) {
                    if (currentVariables == lastVariables) {
                        sameLocalsOneStackFrame(offsetDelta, currentFrame.peek().toVerificationType(constantPoolEditor))
                    } else {
                        null
                    }
                } else {
                    null
                }

            if (frame == null) {
                val currentStack = currentFrame.stack.toVerificationTypeList(constantPoolEditor)
                frame = fullFrame(offsetDelta, currentVariables.toVerificationTypeList(constantPoolEditor), currentStack)
            }

            logger.trace { "offset $offset: adding frame $frame"}

            frameList.add(frame)

            lastOffsetFramePair = Pair(offset, currentFrame)
        }
    }

    private fun trimNonAliveValues(frame: Frame): List<Value> {
        val result = mutableListOf<Pair<Value, Boolean>>()
        var i = 0
        while (i < frame.variables.size) {
            val value = frame.variables[i]
            result.add(Pair(value, frame.isAlive(i)))
            i += value.operandSize
        }

        while (result.isNotEmpty() && !result.last().second) {
            result.removeLast()
        }

        return result.map { it.first }
    }
}
