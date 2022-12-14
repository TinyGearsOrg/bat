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
import org.tinygears.bat.jasm.parser.JasmParser.*
import org.tinygears.bat.jasm.parser.JasmParser.SBaseValueContext
import org.antlr.v4.runtime.tree.TerminalNode
import org.tinygears.bat.classfile.constant.ReferenceKind

internal class ConstantAssembler constructor(private val constantPoolEditor: ConstantPoolEditor) {

    fun parseBaseValue(ctx: SBaseValueContext): Int {

        val value = (ctx.getChild(0) as TerminalNode).symbol

        return when (value.type) {
            STRING  -> constantPoolEditor.addOrGetUtf8ConstantIndex(parseString(value.text))
            BOOLEAN -> constantPoolEditor.addOrGetIntegerConstantIndex(("true" == value.text).toInt())
            BYTE    -> constantPoolEditor.addOrGetIntegerConstantIndex(parseByte(value.text).toInt())
            SHORT   -> constantPoolEditor.addOrGetIntegerConstantIndex(parseShort(value.text).toInt())
            CHAR    -> constantPoolEditor.addOrGetIntegerConstantIndex(parseChar(value.text).code)
            INT     -> constantPoolEditor.addOrGetIntegerConstantIndex(parseInt(value.text))
            LONG    -> constantPoolEditor.addOrGetLongConstantIndex(parseLong(value.text))

            BASE_FLOAT,
            FLOAT_INFINITY,
            FLOAT_NAN   -> constantPoolEditor.addOrGetFloatConstantIndex(parseFloat(value.text))

            BASE_DOUBLE,
            DOUBLE_INFINITY,
            DOUBLE_NAN  -> constantPoolEditor.addOrGetDoubleConstantIndex(parseDouble(value.text))

            ARRAY_TYPE,
            CLASS_NAME  -> constantPoolEditor.addOrGetClassConstantIndex(value.text)

            OBJECT_TYPE -> constantPoolEditor.addOrGetUtf8ConstantIndex(value.text)

            DMETHODTYPE -> {
                val methodDescriptor = (ctx.getChild(1) as TerminalNode).symbol.text
                constantPoolEditor.addOrGetMethodTypeConstantIndex(methodDescriptor)
            }

            DMETHODHANDLE -> {
                val refKind = ReferenceKind.ofSimpleName((ctx.getChild(1) as TerminalNode).symbol.text)

                val methodObj = (ctx.getChild(2) as TerminalNode).symbol.text
                val (className, methodName, descriptor) = parseSimpleMethodObject(methodObj)

                constantPoolEditor.addOrGetMethodHandleConstantIndex(refKind, className!!, methodName, descriptor)
            }

            else -> null
        } ?: error("failed to parse constant base value")
    }
}

private fun Boolean.toInt() = if (this) 1 else 0