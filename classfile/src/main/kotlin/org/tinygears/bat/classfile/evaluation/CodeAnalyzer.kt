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
package org.tinygears.bat.classfile.evaluation

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.Method
import org.tinygears.bat.classfile.attribute.Attribute
import org.tinygears.bat.classfile.attribute.CodeAttribute
import org.tinygears.bat.classfile.attribute.ExceptionEntry
import org.tinygears.bat.classfile.attribute.visitor.MethodAttributeVisitor
import org.tinygears.bat.classfile.constant.DoubleConstant
import org.tinygears.bat.classfile.constant.FloatConstant
import org.tinygears.bat.classfile.constant.LongConstant
import org.tinygears.bat.classfile.constant.Utf8Constant
import org.tinygears.bat.classfile.evaluation.value.ReferenceValue
import org.tinygears.bat.classfile.evaluation.value.UnknownValueFactory
import org.tinygears.bat.classfile.evaluation.value.Value
import org.tinygears.bat.classfile.evaluation.value.ValueFactory
import org.tinygears.bat.classfile.instruction.*
import org.tinygears.bat.classfile.instruction.JvmOpCode.*
import org.tinygears.bat.classfile.instruction.visitor.InstructionVisitor
import org.tinygears.bat.classfile.util.getLoggerFor
import org.tinygears.bat.util.*
import kotlin.collections.ArrayDeque

class CodeAnalyzer private constructor(private val processors: List<FrameProcessor> = emptyList()): MethodAttributeVisitor {

    private var evaluated = Array(0) { false }
    private var status    = Array(0) { 0 }

    private var framesBefore: Array<Frame?> = Array(0) { null }
    private var framesAfter:  Array<Frame?> = Array(0) { null }

    private val blockAnalyser    = BlockAnalyser()
    private val frameUpdater     = FrameUpdater()
    private var livenessAnalyser = LivenessAnalyser()

    private var processingQueue: ArrayDeque<Pair<Int, () -> Unit>> = ArrayDeque()

    private val valueFactory: ValueFactory = UnknownValueFactory()

    private lateinit var logger: Logger

    override fun visitAnyAttribute(classFile: ClassFile, attribute: Attribute) {}

    override fun visitCode(classFile: ClassFile, method: Method, attribute: CodeAttribute) {
        logger = LoggerFactory.getLoggerFor(CodeAnalyzer::class.java, classFile, method)

        logger.info { "evaluating method ${method.getFullExternalMethodSignature(classFile)}" }

        evaluated = Array(attribute.codeLength) { false }
        status    = Array(attribute.codeLength) { 0 }

        framesBefore = Array(attribute.codeLength) { null }
        framesAfter  = Array(attribute.codeLength) { null }

        processingQueue = ArrayDeque()

        evaluateCode(classFile, method, attribute)
    }

    private fun evaluateCode(classFile: ClassFile, method: Method, code: CodeAttribute) {
        val initialFrame = setupInitialFrame(classFile, method)

        livenessAnalyser = LivenessAnalyser()

        for (exceptionEntry in code.exceptionTable.asReversed()) {
            enqueueExceptionHandler(classFile, exceptionEntry)
        }

        enqueueBasicBlock(0, initialFrame)

        while (processingQueue.isNotEmpty()) {
            val (nextBlockOffset, setupFrame) = processingQueue.removeLast()
            if (!evaluated[nextBlockOffset]) {
                // TODO: if the block was already evaluated, check if the stack is consistent
                setupFrame()
                framesBefore[nextBlockOffset]?.resetVariableLiveness()
                evaluateBasicBlock(classFile, method, code, nextBlockOffset)
            }
        }

        livenessAnalyser.finish()

        // after all reachable code has been evaluated, call processors with computed frames.
        for (offset in evaluated.indices) {
            if (evaluated[offset]) {
                val instruction = JvmInstruction.create(code.code, offset)

                for (processor in processors) {
                    processor.handleInstruction(offset, status[offset], instruction, framesBefore[offset]!!, framesAfter[offset]!!)
                }
            }
        }
    }

    private fun enqueueExceptionHandler(classFile: ClassFile, exceptionEntry: ExceptionEntry) {
        logger.debug { "enqueue exception handler at offset ${exceptionEntry.handlerPC}" }

        val handlerPC = exceptionEntry.handlerPC

        val setupFrame: () -> Unit = {
            val frame = framesBefore[exceptionEntry.startPC]!!.copy()
            frame.clearStack()

            val exceptionType = if (exceptionEntry.catchType == 0) {
                "Ljava/lang/Throwable;".asJvmType()
            } else {
                exceptionEntry.getCaughtExceptionClassName(classFile)!!.toJvmType()
            }

            frame.push(createValueFor(exceptionType))
            framesBefore[handlerPC] = frame
        }

        setStatusFlag(handlerPC, BLOCK_ENTRY)
        setStatusFlag(handlerPC, EXCEPTION_HANDLER)

        processingQueue.addLast(Pair(handlerPC, setupFrame))
    }

    private fun enqueueBranchTarget(offset: Int, frame: Frame) {
        logger.debug { "enqueue branch target at offset $offset" }
        setStatusFlag(offset, BRANCH_TARGET)
        enqueueBasicBlock(offset, frame)
    }

    private fun enqueueBasicBlock(offset: Int, frame: Frame) {
        setStatusFlag(offset, BLOCK_ENTRY)
        processingQueue.addLast(Pair(offset) { framesBefore[offset] = frame })
    }

    private fun setupInitialFrame(classFile: ClassFile, method: Method): Frame {
        val descriptor = method.getDescriptor(classFile)
        val (parameterTypes, _) = parseDescriptorToJvmTypes(descriptor)

        val frame = Frame.empty()

        var variableIndex = 0

        if (!method.isStatic) {
            val classType = classFile.className.toJvmType()
            if (method.getName(classFile) == "<init>") {
                frame.store(variableIndex, valueFactory.createUninitializedReferenceValue(classType, -1))
                frame.variableWritten(variableIndex)
            } else {
                frame.store(variableIndex, valueFactory.createReferenceValue(classType))
                frame.variableWritten(variableIndex)
            }
            variableIndex++
        }

        for (parameterType in parameterTypes) {
            val verificationType = createValueFor(parameterType)
            frame.store(variableIndex, verificationType)
            frame.variableWritten(variableIndex)

            variableIndex++
            if (verificationType.isCategory2) {
                variableIndex++
            }
        }

        return frame
    }

    private fun evaluateBasicBlock(classFile: ClassFile, method: Method, attribute: CodeAttribute, offset: Int) {
        var currentOffset = offset

        logger.trace { "starting block at offset $offset" }
        while (!evaluated[currentOffset]) {
            evaluated[currentOffset] = true

            val instruction = JvmInstruction.create(attribute.code, currentOffset)

            instruction.accept(classFile, method, attribute, currentOffset, frameUpdater)
            instruction.accept(classFile, method, attribute, currentOffset, livenessAnalyser)

            logger.trace { "$currentOffset: $instruction" }
            logger.trace { "    before: ${framesBefore[currentOffset]}" }
            logger.trace { "    after: ${framesAfter[currentOffset]}" }

            instruction.accept(classFile, method, attribute, currentOffset, blockAnalyser)

            if (isFlagSet(currentOffset, BLOCK_EXIT)) {
                break
            } else {
                val length     = instruction.getLength(offset)
                val nextOffset = currentOffset + length

                if (!evaluated[nextOffset]) {
                    framesBefore[nextOffset] = framesAfter[currentOffset]
                }

                currentOffset = nextOffset
            }
        }

        logger.trace("finished block")
    }

    inner class FrameUpdater: InstructionVisitor {
        override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {
            TODO("implement ${instruction.opCode.mnemonic}")
        }

        override fun visitBasicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BasicInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                NOP         -> {}

                ACONST_NULL -> frameAfter.push(valueFactory.createNullReferenceValue())

                ATHROW      -> {
                    frameAfter.clearStack()
                    frameAfter.push(frameBefore.peek())
                }

                MONITORENTER,
                MONITOREXIT -> frameAfter.pop()

                else -> error("unexpected opcode ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitStackInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: StackInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                DUP  -> frameAfter.push(frameAfter.peek())

                DUP2 -> {
                    val v1 = frameAfter.pop()
                    if (v1.isCategory2) {
                        frameAfter.push(v1)
                        frameAfter.push(v1)
                    } else {
                        val v2 = frameAfter.pop()

                        frameAfter.push(v2)
                        frameAfter.push(v1)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    }
                }

                DUP_X1 -> {
                    val v1 = frameAfter.pop()
                    val v2 = frameAfter.pop()

                    frameAfter.push(v1)
                    frameAfter.push(v2)
                    frameAfter.push(v1)
                }

                DUP_X2 -> {
                    val v1 = frameAfter.pop()
                    if (v1.isCategory2) {
                        val v2 = frameAfter.pop()

                        frameAfter.push(v1)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    } else {
                        val v2 = frameAfter.pop()
                        val v3 = frameAfter.pop()

                        frameAfter.push(v1)
                        frameAfter.push(v3)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    }
                }

                DUP2_X1 -> {
                    val v1 = frameAfter.pop()
                    if (v1.isCategory2) {
                        val v2 = frameAfter.pop()

                        frameAfter.push(v1)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    } else {
                        val v2 = frameAfter.pop()
                        val v3 = frameAfter.pop()

                        frameAfter.push(v2)
                        frameAfter.push(v1)
                        frameAfter.push(v3)
                        frameAfter.push(v2)
                        frameAfter.push(v1)
                    }
                }

                SWAP -> {
                    val v1 = frameAfter.pop()
                    val v2 = frameAfter.pop()

                    frameAfter.push(v1)
                    frameAfter.push(v2)
                }

                POP  -> frameAfter.pop()
                POP2 -> {
                    val topValue = frameAfter.pop()
                    if (!topValue.isCategory2) {
                        frameAfter.pop()
                    }
                }

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitLiteralVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralVariableInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                IINC -> {}
                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            if (instruction.isLoadInstruction) {
                frameAfter.push(frameAfter.load(instruction.variable))
            } else if (instruction.isStoreInstruction) {
                frameAfter.store(instruction.variable, frameAfter.pop())
            } else {
                TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitLiteralConstantInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralConstantInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.getConstant(classFile)) {
                is DoubleConstant -> frameAfter.push(valueFactory.createDoubleValue())
                is LongConstant   -> frameAfter.push(valueFactory.createLongValue())
                is FloatConstant  -> frameAfter.push(valueFactory.createFloatValue())
                is Utf8Constant   -> frameAfter.push(valueFactory.createReferenceValue(JAVA_LANG_STRING_TYPE))
                else              -> frameAfter.push(valueFactory.createIntegerValue())
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitLiteralInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: LiteralInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                DCONST_0,
                DCONST_1 -> frameAfter.push(valueFactory.createDoubleValue())

                FCONST_0,
                FCONST_1,
                FCONST_2 -> frameAfter.push(valueFactory.createFloatValue())

                LCONST_0,
                LCONST_1 -> frameAfter.push(valueFactory.createLongValue())

                else     -> frameAfter.push(valueFactory.createIntegerValue())
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArrayInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                ARRAYLENGTH -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createIntegerValue())
                }

                AASTORE,
                BASTORE,
                SASTORE,
                CASTORE,
                LASTORE,
                DASTORE,
                FASTORE,
                IASTORE -> frameAfter.pop(3)

                AALOAD -> {
                    frameAfter.pop()
                    val arrayType = frameAfter.pop()
                    check(arrayType is ReferenceValue && arrayType.type.isArrayType)
                    val componentType = arrayType.type.componentType
                    frameAfter.push(valueFactory.createReferenceValue(componentType))
                }

                BALOAD,
                SALOAD,
                CALOAD,
                IALOAD  -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createIntegerValue())
                }

                FALOAD -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createFloatValue())
                }

                DALOAD -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createDoubleValue())
                }

                LALOAD -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createLongValue())
                }

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArrayPrimitiveTypeInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayPrimitiveTypeInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                NEWARRAY -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createReferenceValue(instruction.arrayType))
                }

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArrayClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArrayClassInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val classType = instruction.getClassName(classFile).toJvmType()
            val arrayType = "[${classType.type}".asJvmType()

            when (instruction.opCode) {
                ANEWARRAY -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createReferenceValue(arrayType))
                }

                MULTIANEWARRAY -> {
                    val dimensions = instruction.dimension
                    frameAfter.pop(dimensions)
                    frameAfter.push(valueFactory.createReferenceValue(instruction.getClassName(classFile).toJvmType()))
                }

                else -> error("unexpected opcode '${instruction.opCode}'")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitClassInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ClassInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val classType = instruction.getClassName(classFile).toJvmType()

            when (instruction.opCode) {
                NEW        -> frameAfter.push(valueFactory.createUninitializedReferenceValue(classType, offset))
                CHECKCAST  -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createReferenceValue(classType))
                }
                INSTANCEOF -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createIntegerValue())
                }
                else       -> error("unexpected opcode '${instruction.opCode}'")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitFieldInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: FieldInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val fieldType = instruction.getDescriptor(classFile).asJvmType()

            when (instruction.opCode) {
                GETFIELD -> {
                    frameAfter.pop()
                    frameAfter.push(createValueFor(fieldType))
                }

                GETSTATIC -> {
                    frameAfter.push(createValueFor(fieldType))
                }

                PUTFIELD  -> frameAfter.pop(2)
                PUTSTATIC -> frameAfter.pop()

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: MethodInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val (parameterTypes, returnType) = parseDescriptorToJvmTypes(instruction.getDescriptor(classFile))
            frameAfter.pop(parameterTypes.size)

            when (instruction.opCode) {
                INVOKESTATIC  -> {}
                INVOKESPECIAL -> {
                    val isInitializer = instruction.getMethodName(classFile) == "<init>"
                    if (isInitializer) {
                        val objectReference = frameAfter.pop()
                        check(objectReference is ReferenceValue)
                        frameAfter.referenceInitialized(objectReference, valueFactory.createReferenceValue(objectReference.type))
                    } else {
                        frameAfter.pop()
                    }
                }
                else -> frameAfter.pop()
            }


            if (!returnType.isVoidType) {
                frameAfter.push(createValueFor(returnType))
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitInterfaceMethodInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InterfaceMethodInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val (parameterTypes, returnType) = parseDescriptorToJvmTypes(instruction.getDescriptor(classFile))
            frameAfter.pop(parameterTypes.size)
            frameAfter.pop()

            if (!returnType.isVoidType) {
                frameAfter.push(createValueFor(returnType))
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitInvokeDynamicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: InvokeDynamicInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            val (parameterTypes, returnType) = parseDescriptorToJvmTypes(instruction.getDescriptor(classFile))
            frameAfter.pop(parameterTypes.size)

            if (!returnType.isVoidType) {
                frameAfter.push(createValueFor(returnType))
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                IF_ACMPEQ,
                IF_ACMPNE -> frameAfter.pop(2)

                IF_ICMPEQ,
                IF_ICMPGE,
                IF_ICMPNE,
                IF_ICMPLE,
                IF_ICMPGT,
                IF_ICMPLT -> frameAfter.pop(2)

                IFEQ,
                IFLT,
                IFGE,
                IFLE,
                IFNE,
                IFGT,
                IFNULL,
                IFNONNULL -> frameAfter.pop()

                GOTO,
                GOTO_W -> {}

                else -> TODO("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitConversionInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ConversionInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                F2I,
                L2I,
                D2I,
                I2B,
                I2S,
                I2C -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createIntegerValue())
                }

                D2L,
                F2L,
                I2L -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createLongValue())
                }

                L2D,
                F2D,
                I2D -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createDoubleValue())
                }

                I2F,
                D2F,
                L2F -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createFloatValue())
                }

                else -> error("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitCompareInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: CompareInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                FCMPG,
                FCMPL,
                DCMPG,
                DCMPL,
                LCMP -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createIntegerValue())
                }

                else -> error("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitArithmeticInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ArithmeticInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                IAND,
                IOR,
                IXOR,
                IADD,
                ISUB,
                ISHL,
                ISHR,
                IUSHR,
                IREM,
                IMUL,
                IDIV, -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createIntegerValue())
                }

                INEG -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createIntegerValue())
                }

                LNEG -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createLongValue())
                }

                DNEG -> {
                    frameAfter.pop()
                    frameAfter.push(valueFactory.createDoubleValue())
                }

                LOR,
                LXOR,
                LAND,
                LADD,
                LSUB,
                LDIV,
                LMUL,
                LSHL,
                LUSHR,
                LREM,
                LSHR -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createLongValue())
                }

                FADD,
                FSUB,
                FMUL,
                FREM,
                FDIV -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createFloatValue())
                }

                DSUB,
                DADD,
                DMUL,
                DREM,
                DDIV -> {
                    frameAfter.pop(2)
                    frameAfter.push(valueFactory.createDoubleValue())
                }

                else -> error("implement ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitAnySwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: SwitchInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                LOOKUPSWITCH,
                TABLESWITCH -> frameAfter.pop()
                else        -> error("unexpected opcode ${instruction.opCode}")
            }

            framesAfter[offset] = frameAfter
        }

        override fun visitReturnInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ReturnInstruction) {
            val frameBefore = framesBefore[offset]
            val frameAfter  = frameBefore!!.copy()

            when (instruction.opCode) {
                RETURN -> {}
                else   -> frameAfter.pop()
            }

            framesAfter[offset] = frameAfter
        }
    }

    fun createValueFor(jvmType: JvmType): Value {
        return if (jvmType.isReferenceType) {
            valueFactory.createReferenceValue(jvmType)
        } else if (jvmType.isPrimitiveType) {
            when (jvmType.type) {
                BYTE_TYPE,
                SHORT_TYPE,
                CHAR_TYPE,
                BOOLEAN_TYPE,
                INT_TYPE    -> valueFactory.createIntegerValue()
                FLOAT_TYPE  -> valueFactory.createFloatValue()
                LONG_TYPE   -> valueFactory.createLongValue()
                DOUBLE_TYPE -> valueFactory.createDoubleValue()
                else        -> error("unexpected primitive type '$jvmType'")
            }
        } else {
            error("unexpected type '$jvmType'")
        }
    }

    inner class BlockAnalyser: InstructionVisitor {
        override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

        override fun visitAnySwitchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: SwitchInstruction) {
            setStatusFlag(offset, BLOCK_EXIT)

            val frameAfter = framesAfter[offset]!!

            val defaultOffset = offset + instruction.defaultOffset
            enqueueBranchTarget(defaultOffset, frameAfter)

            for (matchOffsetPair in instruction) {
                val targetOffset = offset + matchOffsetPair.offset
                enqueueBranchTarget(targetOffset, frameAfter)
            }
        }

        override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
            setStatusFlag(offset, BLOCK_EXIT)

            val targetOffset = offset + instruction.branchOffset
            val nextOffset   = offset + instruction.getLength(offset)

            val frameAfter = framesAfter[offset]!!

            when (instruction.opCode) {
                GOTO,
                GOTO_W -> {
                    enqueueBranchTarget(targetOffset, frameAfter)
                }

                else -> {
                    enqueueBasicBlock(nextOffset, frameAfter)
                    enqueueBranchTarget(targetOffset, frameAfter)
                }
            }
        }

        override fun visitBasicInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BasicInstruction) {
            if (instruction.opCode == ATHROW) {
                setStatusFlag(offset, BLOCK_EXIT)
            }
        }

        override fun visitReturnInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: ReturnInstruction) {
            setStatusFlag(offset, BLOCK_EXIT)
        }
    }

    inner class LivenessAnalyser: InstructionVisitor {
        private var postProcessing = mutableListOf<Pair<Int, Int>>()

        fun finish() {
            for ((sourceOffset, targetOffset) in postProcessing) {
                logger.debug("postprocessing: $sourceOffset -> $targetOffset")
                framesAfter[targetOffset]?.mergeLiveness(framesBefore[sourceOffset]!!)
            }
        }

        override fun visitAnyInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: JvmInstruction) {}

        override fun visitVariableInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: VariableInstruction) {
            val frameBefore = framesBefore[offset]!!
            val frameAfter  = framesAfter[offset]!!

            if (instruction.isLoadInstruction) {
                frameBefore.variableRead(instruction.variable)
                frameAfter.resetVariableLiveness(instruction.variable)
            } else if (instruction.isStoreInstruction) {
                frameAfter.variableWritten(instruction.variable)
            }
        }

        override fun visitBranchInstruction(classFile: ClassFile, method: Method, code: CodeAttribute, offset: Int, instruction: BranchInstruction) {
            val targetOffset = offset + instruction.branchOffset
            val nextOffset   = offset + instruction.getLength(offset)

            val frameAfter = framesAfter[offset]!!

            when (instruction.opCode) {
                GOTO,
                GOTO_W -> {
                    postProcessing.add(Pair(targetOffset, offset))
                }

                else -> {
                    postProcessing.add(Pair(nextOffset, offset))
                    postProcessing.add(Pair(targetOffset, offset))
                }
            }
        }
    }

    private fun setStatusFlag(offset: Int, flag: Int) {
        status[offset] = status[offset] or flag
    }

    private fun isFlagSet(offset: Int, flag: Int): Boolean {
        return (status[offset] and flag) != 0
    }

    companion object {
        const val BLOCK_ENTRY       = 1 shl 1
        const val BLOCK_EXIT        = 1 shl 2
        const val EXCEPTION_HANDLER = 1 shl 3
        const val BRANCH_TARGET     = 1 shl 4

        fun withProcessors(vararg processors: FrameProcessor): CodeAnalyzer {
            return CodeAnalyzer(processors.toList())
        }
    }
}
