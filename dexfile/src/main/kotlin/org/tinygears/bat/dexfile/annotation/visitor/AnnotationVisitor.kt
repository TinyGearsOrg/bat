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
package org.tinygears.bat.dexfile.annotation.visitor

import org.tinygears.bat.dexfile.DexFile
import org.tinygears.bat.dexfile.annotation.Annotation
import org.tinygears.bat.visitor.AbstractCollector
import org.tinygears.bat.visitor.AbstractMultiVisitor
import java.util.function.BiConsumer

fun annotationCollector(): AnnotationCollector {
    return AnnotationCollector()
}

fun multiAnnotationVisitorOf(visitor: AnnotationVisitor, vararg visitors: AnnotationVisitor): AnnotationVisitor {
    return MultiAnnotationVisitor(visitor, *visitors)
}

fun interface AnnotationVisitor {
    fun visitAnnotation(dexFile: DexFile, annotation: Annotation)

    fun joinedByAnnotationConsumer(consumer: BiConsumer<DexFile, Annotation>): AnnotationVisitor {
        val joiner: AnnotationVisitor = object : AnnotationVisitor {
            private var firstVisited = false
            override fun visitAnnotation(dexFile: DexFile, annotation: Annotation) {
                if (firstVisited) {
                    consumer.accept(dexFile, annotation)
                } else {
                    firstVisited = true
                }
            }
        }
        return multiAnnotationVisitorOf(joiner, this)
    }
}

class AnnotationCollector: AbstractCollector<Annotation>(), AnnotationVisitor {
    override fun visitAnnotation(dexFile: DexFile, annotation: Annotation) {
        addItem(annotation)
    }
}

private class MultiAnnotationVisitor constructor(       visitor:       AnnotationVisitor,
                                                 vararg otherVisitors: AnnotationVisitor)
    : AbstractMultiVisitor<AnnotationVisitor>(visitor, *otherVisitors), AnnotationVisitor {

    override fun visitAnnotation(dexFile: DexFile, annotation: Annotation) {
        for (visitor in visitors) {
            visitor.visitAnnotation(dexFile, annotation)
        }
    }
}