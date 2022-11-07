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

package org.tinygears.bat.dexfile.editor

import org.tinygears.bat.dexfile.DexFile
import org.tinygears.bat.dexfile.EncodedField
import org.tinygears.bat.dexfile.annotation.Annotation
import org.tinygears.bat.dexfile.value.EncodedValue

class FieldEditor private constructor(val dexEditor: DexEditor, val classDefEditor: ClassDefEditor, val field: EncodedField) {

    private val dexFile: DexFile
        get() = dexEditor.dexFile

    fun setStaticValue(value: EncodedValue) {
        classDefEditor.setStaticValue(field, value)
    }

    fun addAnnotation(annotation: Annotation) {
        val annotationSet = classDefEditor.addOrGetFieldAnnotationSet(field)
        annotationSet.addAnnotation(dexFile, annotation)
    }

    companion object {
        fun of(dexEditor: DexEditor, classDefEditor: ClassDefEditor, field: EncodedField): FieldEditor {
            return FieldEditor(dexEditor, classDefEditor, field)
        }
    }
}