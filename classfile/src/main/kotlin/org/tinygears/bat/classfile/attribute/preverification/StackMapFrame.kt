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

package org.tinygears.bat.classfile.attribute.preverification

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.attribute.preverification.visitor.StackMapFrameVisitor
import org.tinygears.bat.classfile.constant.visitor.ReferencedConstantVisitor
import org.tinygears.bat.classfile.io.*
import org.tinygears.bat.classfile.io.ClassDataInput
import org.tinygears.bat.classfile.io.ClassDataOutput
import org.tinygears.bat.util.mutableListOfCapacity

fun sameFrame(offsetDelta: Int): StackMapFrame {
    return if (offsetDelta > SameFrame.MAX_OFFSET) {
        SameFrameExtended.of(offsetDelta)
    } else {
        SameFrame.of(offsetDelta)
    }
}

fun sameFrameOneStack(offsetDelta: Int, stackItem: VerificationType): StackMapFrame {
    return if (offsetDelta in 0 .. SameLocalsOneStackItemFrame.MAX_OFFSET) {
        SameLocalsOneStackItemFrame.of(offsetDelta, stackItem)
    } else {
        SameLocalsOneStackItemFrameExtended.of(offsetDelta, stackItem)
    }
}

fun appendFrame(offsetDelta: Int, lastVariables: List<VerificationType>, currentVariables: List<VerificationType>): StackMapFrame {
    val appendedVariables = currentVariables.size - lastVariables.size
    return if (appendedVariables in 1 .. 3) {
        val size = currentVariables.size
        AppendFrame.of(offsetDelta, currentVariables.subList(size - appendedVariables, size))
    } else {
        FullFrame.of(offsetDelta, currentVariables, emptyList())
    }
}

fun chopFrame(offsetDelta: Int, choppedVariables: Int, variables: List<VerificationType>): StackMapFrame {
    return if (choppedVariables in 1 .. 3) {
        ChopFrame.of(offsetDelta, choppedVariables)
    } else {
        FullFrame.of(offsetDelta, variables, emptyList())
    }
}

fun fullFrame(offsetDelta: Int, variables: List<VerificationType>, stack: List<VerificationType>): StackMapFrame {
    return FullFrame.of(offsetDelta, variables, stack)
}

abstract class StackMapFrame protected constructor(val frameType: Int): ClassFileContent() {
    internal abstract val type: StackMapFrameType

    abstract val offsetDelta: Int

    abstract val verificationTypes: List<VerificationType>

    internal open fun readData(input: ClassDataInput) {}
    internal open fun writeData(output: ClassDataOutput) {}

    override fun write(output: ClassDataOutput) {
        output.writeByte(frameType)
        writeData(output)
    }

    abstract fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor)

    open fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {}

    abstract override fun toString(): String

    companion object {
        internal fun read(input: ClassDataInput): StackMapFrame {
            val frameType = input.readUnsignedByte()

            val stackMapFrame = StackMapFrameType.of(frameType)
            stackMapFrame.readData(input)
            return stackMapFrame
        }
    }
}

class SameFrame private constructor(frameType: Int): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_FRAME

    override val contentSize: Int
        get() = 1

    override val offsetDelta: Int
        get() = frameType

    override val verificationTypes: List<VerificationType>
        get() = emptyList()

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameFrame(classFile, this)
    }

    override fun toString(): String {
        return "SameFrame($offsetDelta)"
    }

    companion object {
        const val MAX_OFFSET = 63

        internal fun create(frameType: Int): SameFrame {
            require(frameType in 0 .. MAX_OFFSET)
            return SameFrame(frameType)
        }

        fun of(offsetDelta: Int): SameFrame {
            require(offsetDelta in 0 .. MAX_OFFSET) { "offsetDelta must be in the range of [0, 63]" }
            return SameFrame(offsetDelta)
        }
    }
}

class ChopFrame private constructor(            frameType:    Int,
                                    private var _offsetDelta: Int = 0): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.CHOP_FRAME

    override val contentSize: Int
        get() = 3

    override val offsetDelta: Int
        get() = _offsetDelta

    val choppedVariables: Int
        get() = 251 - frameType

    override val verificationTypes: List<VerificationType>
        get() = emptyList()

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(offsetDelta)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitChopFrame(classFile, this)
    }

    override fun toString(): String {
        return "ChopFrame($offsetDelta,$choppedVariables)"
    }

    companion object {
        internal fun create(frameType: Int): ChopFrame {
            require(frameType in 248 .. 250)
            return ChopFrame(frameType)
        }

        fun of(offsetDelta: Int, choppedVariables: Int): ChopFrame {
            require(offsetDelta >= 0) { "offsetDelta must not be negative" }
            require(choppedVariables in 1 .. 3) { "number of chopped variables must be in the range of [1, 3]"}
            return ChopFrame(251 - choppedVariables, offsetDelta)
        }
    }
}

class SameFrameExtended private constructor(            frameType:    Int,
                                            private var _offsetDelta: Int = 0): StackMapFrame(frameType) {
    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_FRAME_EXTENDED

    override val contentSize: Int
        get() = 3

    override val offsetDelta: Int
        get() = _offsetDelta

    override val verificationTypes: List<VerificationType>
        get() = emptyList()

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(offsetDelta)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameFrameExtended(classFile, this)
    }

    override fun toString(): String {
        return "SameFrameExtended($offsetDelta)"
    }

    companion object {
        internal fun create(frameType: Int): SameFrameExtended {
            require(frameType == 251)
            return SameFrameExtended(frameType)
        }

        fun of(offsetDelta: Int): SameFrameExtended {
            require(offsetDelta >= 0) { "offsetDelta must not be negative" }
            return SameFrameExtended(251, offsetDelta)
        }
    }
}

class AppendFrame private constructor(            frameType:    Int,
                                      private var _offsetDelta: Int = 0,
                                      private var locals:       MutableList<VerificationType> = mutableListOfCapacity(0))
    : StackMapFrame(frameType), Sequence<VerificationType> {

    override val type: StackMapFrameType
        get() = StackMapFrameType.APPEND_FRAME

    override val contentSize: Int
        get() = 3 + locals.fold(0) { acc, element -> acc + element.contentSize }

    override val offsetDelta: Int
        get() = _offsetDelta

    val appendedVariables: Int
        get() = frameType - 251

    val size: Int
        get() = locals.size

    operator fun get(index: Int): VerificationType {
        return locals[index]
    }

    override fun iterator(): Iterator<VerificationType> {
        return locals.iterator()
    }

    override val verificationTypes: List<VerificationType>
        get() = locals

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
        locals = mutableListOfCapacity(appendedVariables)
        for (i in 0 until appendedVariables) {
            locals.add(VerificationType.read(input))
        }
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(offsetDelta)
        for (element in locals) {
            element.write(output)
        }
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitAppendFrame(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        for (local in locals) {
            local.referencedConstantsAccept(classFile, visitor)
        }
    }

    override fun toString(): String {
        return "AppendFrame($offsetDelta,$appendedVariables,$locals)"
    }

    companion object {
        internal fun create(frameType: Int): AppendFrame {
            require(frameType in 252 .. 254)
            return AppendFrame(frameType)
        }

        fun of(offsetDelta: Int, localTypes: List<VerificationType>): AppendFrame {
            require(offsetDelta >= 0) { "offsetDelta must not be negative" }
            require(localTypes.size in 1 .. 3) { "number of added variables must be in the range of [1, 3]"}
            return AppendFrame(251 + localTypes.size, offsetDelta, localTypes.toMutableList())
        }
    }
}

class SameLocalsOneStackItemFrame private constructor(            frameType: Int,
                                                      private var _stack:    VerificationType = TopVariable.empty())
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_LOCALS_1_STACK_ITEM_FRAME

    override val contentSize: Int
        get() = 1 + _stack.contentSize

    override val offsetDelta: Int
        get() = frameType - 64

    val stackItem: VerificationType
        get() = _stack

    override val verificationTypes: List<VerificationType>
        get() = listOf(stackItem)

    override fun readData(input: ClassDataInput) {
        _stack = VerificationType.read(input)
    }

    override fun writeData(output: ClassDataOutput) {
        _stack.write(output)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameLocalsOneStackItemFrame(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        _stack.referencedConstantsAccept(classFile, visitor)
    }

    override fun toString(): String {
        return "SameLocalsOneStackItemFrame($offsetDelta,$stackItem)"
    }

    companion object {
        const val MAX_OFFSET = 63

        internal fun create(frameType: Int): SameLocalsOneStackItemFrame {
            require(frameType in 64 .. 127)
            return SameLocalsOneStackItemFrame(frameType)
        }

        fun of(offsetDelta: Int, stackItem: VerificationType): SameLocalsOneStackItemFrame {
            require(offsetDelta in 0 .. MAX_OFFSET) { "offsetDelta must be in the range of [0, 64]" }
            return SameLocalsOneStackItemFrame(offsetDelta + 64, stackItem)
        }
    }
}

class SameLocalsOneStackItemFrameExtended private constructor(            frameType:    Int,
                                                              private var _offsetDelta: Int = 0,
                                                              private var _stack:       VerificationType = TopVariable.empty())
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED

    override val contentSize: Int
        get() = 3 + _stack.contentSize

    override val offsetDelta: Int
        get() = _offsetDelta

    val stackItem: VerificationType
        get() = _stack

    override val verificationTypes: List<VerificationType>
        get() = listOf(stackItem)

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
        _stack = VerificationType.read(input)
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(_offsetDelta)
        _stack.write(output)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitSameLocalsOneStackItemFrameExtended(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        _stack.referencedConstantsAccept(classFile, visitor)
    }

    override fun toString(): String {
        return "SameFrameOneStackItemFrameExtended($offsetDelta,$stackItem)"
    }

    companion object {
        internal fun create(frameType: Int): SameLocalsOneStackItemFrameExtended {
            require(frameType == 247)
            return SameLocalsOneStackItemFrameExtended(frameType)
        }

        fun of(offsetDelta: Int, stackItem: VerificationType): SameLocalsOneStackItemFrameExtended {
            require(offsetDelta >= 0) { "offsetDelta must not be negative" }
            return SameLocalsOneStackItemFrameExtended(247, offsetDelta, stackItem)
        }
    }
}

class FullFrame private constructor(            frameType:    Int,
                                    private var _offsetDelta: Int = 0,
                                    private var _locals:      MutableList<VerificationType> = mutableListOfCapacity(0),
                                    private var _stack:       MutableList<VerificationType> = mutableListOfCapacity(0))
    : StackMapFrame(frameType) {

    override val type: StackMapFrameType
        get() = StackMapFrameType.FULL_FRAME

    override val contentSize: Int
        get() = 3 + locals.contentSize() + stack.contentSize()

    override val offsetDelta: Int
        get() = _offsetDelta

    val locals: List<VerificationType>
        get() = _locals

    val stack: List<VerificationType>
        get() = _stack

    override val verificationTypes: List<VerificationType>
        get() = locals + stack

    override fun readData(input: ClassDataInput) {
        _offsetDelta = input.readUnsignedShort()
        _locals = input.readContentList(VerificationType.Companion::read)
        _stack  = input.readContentList(VerificationType.Companion::read)
    }

    override fun writeData(output: ClassDataOutput) {
        output.writeShort(_offsetDelta)
        output.writeContentList(_locals)
        output.writeContentList(_stack)
    }

    override fun accept(classFile: ClassFile, visitor: StackMapFrameVisitor) {
        visitor.visitFullFrame(classFile, this)
    }

    override fun referencedConstantsAccept(classFile: ClassFile, visitor: ReferencedConstantVisitor) {
        for (local in _locals) {
            local.referencedConstantsAccept(classFile, visitor)
        }

        for (stackEntry in _stack) {
            stackEntry.referencedConstantsAccept(classFile, visitor)
        }
    }

    override fun toString(): String {
        return "FullFrame($offsetDelta,$locals,$stack)"
    }

    companion object {
        internal fun create(frameType: Int): FullFrame {
            require(frameType == 255)
            return FullFrame(frameType)
        }

        fun of(offsetDelta: Int, locals: List<VerificationType>, stack: List<VerificationType>): FullFrame {
            require(offsetDelta >= 0) { "offsetDelta must not be negative" }
            return FullFrame(255, offsetDelta, locals.toMutableList(), stack.toMutableList())
        }
    }
}

internal enum class StackMapFrameType constructor(private val supplier: (Int) -> StackMapFrame) {
    SAME_FRAME                             ({ SameFrame.create(it) }),
    SAME_LOCALS_1_STACK_ITEM_FRAME         ({ SameLocalsOneStackItemFrame.create(it) }),
    SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED({ SameLocalsOneStackItemFrameExtended.create(it) }),
    CHOP_FRAME                             ({ ChopFrame.create(it) }),
    SAME_FRAME_EXTENDED                    ({ SameFrameExtended.create(it) }),
    APPEND_FRAME                           ({ AppendFrame.create(it) }),
    FULL_FRAME                             ({ FullFrame.create(it) });

    companion object {
        fun of(frameType: Int) : StackMapFrame {
            val type = when (frameType) {
                in 0 .. 63    -> SAME_FRAME
                in 64 .. 127  -> SAME_LOCALS_1_STACK_ITEM_FRAME
                247           -> SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED
                in 248 .. 250 -> CHOP_FRAME
                251           -> SAME_FRAME_EXTENDED
                in 252 .. 254 -> APPEND_FRAME
                255           -> FULL_FRAME
                else -> error("unexpected frameType '$frameType'")
            }

            return type.supplier(frameType)
        }
    }
}
