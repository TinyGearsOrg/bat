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

package org.tinygears.bat.jasm.assemble

import org.tinygears.bat.classfile.constant.editor.ConstantPoolEditor
import org.tinygears.bat.jasm.parser.JasmLexer
import org.tinygears.bat.jasm.parser.JasmParser.*
import org.antlr.v4.runtime.tree.TerminalNode
import org.tinygears.bat.classfile.attribute.annotation.*

internal class ElementValueAssembler
    constructor(private val constantPoolEditor: ConstantPoolEditor,
                private val constantAssembler:  ConstantAssembler) {

    fun parseBaseValue(ctx: SBaseValueContext): ElementValue {

        // a base value is usually only a single token, only exception: enum fields
        val (value, isEnum) = if (ctx.childCount == 1) {
            val tn = ctx.getChild(0) as TerminalNode
            Pair(tn.symbol, false)
        } else {
            // in case of an enum, the first child is the ".enum" fragment.
            val first = (ctx.getChild(0) as TerminalNode).symbol
            assert(first.type == JasmLexer.DENUM)

            val tn = ctx.getChild(1) as TerminalNode
            Pair(tn.symbol, true)
        }

        return when (value.type) {
            STRING ->        ConstElementValue.of(ElementValueType.STRING,  constantAssembler.parseBaseValue(ctx))
            BOOLEAN ->       ConstElementValue.of(ElementValueType.BOOLEAN, constantAssembler.parseBaseValue(ctx))
            BYTE ->          ConstElementValue.of(ElementValueType.BYTE,    constantAssembler.parseBaseValue(ctx))
            SHORT ->         ConstElementValue.of(ElementValueType.SHORT,   constantAssembler.parseBaseValue(ctx))
            CHAR ->          ConstElementValue.of(ElementValueType.CHAR,    constantAssembler.parseBaseValue(ctx))
            INT ->           ConstElementValue.of(ElementValueType.INT,     constantAssembler.parseBaseValue(ctx))
            LONG ->          ConstElementValue.of(ElementValueType.LONG,    constantAssembler.parseBaseValue(ctx))

            BASE_FLOAT,
            FLOAT_INFINITY,
            FLOAT_NAN ->     ConstElementValue.of(ElementValueType.FLOAT,   constantAssembler.parseBaseValue(ctx))

            BASE_DOUBLE,
            DOUBLE_INFINITY,
            DOUBLE_NAN ->    ConstElementValue.of(ElementValueType.DOUBLE,  constantAssembler.parseBaseValue(ctx))

            ENUM_FULL -> {
                val (classType, fieldName) = parseEnumObject(value.text)
                val typeNameIndex  = constantPoolEditor.addOrGetUtf8ConstantIndex(classType!!)
                val constNameIndex = constantPoolEditor.addOrGetUtf8ConstantIndex(fieldName)

                EnumElementValue.of(typeNameIndex, constNameIndex)
            }

            OBJECT_TYPE ->   ClassElementValue.of(constantAssembler.parseBaseValue(ctx))

            else -> null
        } ?: parserError(ctx, "failure to parse base value")
    }
}

