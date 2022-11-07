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

package org.tinygears.bat.smali.assemble

import org.tinygears.bat.dexfile.editor.DexEditor
import org.tinygears.bat.dexfile.value.*
import org.antlr.v4.runtime.tree.TerminalNode
import org.tinygears.bat.smali.parser.SmaliParser.*

internal class EncodedValueAssembler constructor(private val dexEditor: DexEditor) {

    fun parseBaseValue(ctx: SBaseValueContext): EncodedValue {

        // a base value is usually only a single token, only exception: enum fields
        val (value, isEnum) = if (ctx.childCount == 1) {
            val tn = ctx.getChild(0) as TerminalNode
            Pair(tn.symbol, false)
        } else {
            // in case of an enum, the first child is the ".enum" fragment.
            val first = (ctx.getChild(0) as TerminalNode).symbol
            assert(first.type == DENUM)

            val tn = ctx.getChild(1) as TerminalNode
            Pair(tn.symbol, true)
        }

        return when (value.type) {
            STRING          -> EncodedStringValue.of(dexEditor.addOrGetStringIDIndex(parseString(value.text)))
            BOOLEAN         -> EncodedBooleanValue.of("true" == value.text)
            BYTE            -> EncodedByteValue.of(parseByte(value.text))
            SHORT           -> EncodedShortValue.of(parseShort(value.text))
            CHAR            -> EncodedCharValue.of(parseChar(value.text))
            INT             -> EncodedIntValue.of(parseInt(value.text))
            LONG            -> EncodedLongValue.of(parseLong(value.text))
            BASE_FLOAT,
            FLOAT_INFINITY,
            FLOAT_NAN       -> EncodedFloatValue.of(parseFloat(value.text))
            BASE_DOUBLE,
            DOUBLE_INFINITY,
            DOUBLE_NAN      -> EncodedDoubleValue.of(parseDouble(value.text))
            METHOD_FULL     -> {
                val (classType, methodName, parameterTypes, returnType) = parseMethodObject(value.text)
                val methodIndex = dexEditor.addOrGetMethodIDIndex(classType!!, methodName, parameterTypes, returnType)
                EncodedMethodValue.of(methodIndex)
            }
            METHOD_PROTO    -> {
                val (_, _, parameterTypes, returnType) = parseMethodObject(value.text)
                val protoIndex = dexEditor.addOrGetProtoIDIndex(parameterTypes, returnType)
                EncodedMethodTypeValue.of(protoIndex)
            }
            FIELD_FULL      -> {
                val (classType, fieldName, type) = parseFieldObject(value.text)
                val fieldIndex = dexEditor.addOrGetFieldIDIndex(classType!!, fieldName, type)

                if (isEnum) EncodedEnumValue.of(fieldIndex) else EncodedFieldValue.of(fieldIndex)
            }
            OBJECT_TYPE     -> EncodedTypeValue.of(dexEditor.addOrGetTypeIDIndex(value.text))
            NULL            -> EncodedNullValue
            else            -> null
        } ?: parserError(ctx, "failure to parse base value")
    }
}