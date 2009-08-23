/*
 * [The "BSD licence"]
 * Copyright (c) 2009 Ben Gruver
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib;

import org.jf.dexlib.EncodedValue.AnnotationEncodedSubValue;
import org.jf.dexlib.Util.ArrayUtils;
import org.jf.dexlib.Util.Input;
import org.jf.dexlib.Util.AnnotatedOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotationDirectoryItem extends Item<AnnotationDirectoryItem> {
    private AnnotationSetItem classAnnotations;

    private FieldIdItem[] fieldAnnotationFields;
    private AnnotationSetItem[] fieldAnnotations;

    private MethodIdItem[] methodAnnotationMethods;
    private AnnotationSetItem[] methodAnnotations;

    private MethodIdItem[] parameterAnnotationMethods;
    private AnnotationSetRefList[] parameterAnnotations;

    /**
     * typically each AnnotationDirectoryItem will have a distinct parent. The only case that isn't true is when
     * the AnnotationDirectoryItem *only* contains class annotations, with no other type of annotation. In that
     * case, the same AnnotationDirectoryItem could be referenced from multiple classes.
     * This isn't a problem though, because this field is only used in compareTo to determine the sort order,
     * which handles it as a special case
     */
    private ClassDefItem parent = null;
                                             
    /**
     * Creates a new uninitialized <code>AnnotationDirectoryItem</code>
     * @param dexFile The <code>DexFile</code> that this item belongs to
     */
    protected AnnotationDirectoryItem(DexFile dexFile) {
        super(dexFile);
    }

    /**
     * Creates a new <code>AnnotationDirectoryItem</code> with the given values
     * @param dexFile The <code>DexFile</code> that this item belongs to
     * @param classAnnotations The annotations associated with the overall class
     * @param fieldAnnotationFields An array of <code>FieldIdItem</code> objects that the annotations in
     * <code>fieldAnnotations</code> are associated with
     * @param fieldAnnotations An array of <code>AnnotationSetItem</code> objects that contain the annotations for the
     * fields in <code>fieldAnnotationFields</code>
     * @param methodAnnotationMethods An array of <code>MethodIdItem</code> objects that the annotations in
     * <code>methodAnnotations</code> are associated with
     * @param methodAnnotations An array of <code>AnnotationSetItem</code> objects that contain the annotations for the
     * methods in <code>methodAnnotationMethods</code>
     * @param parameterAnnotationMethods An array of <code>MethodIdItem</code> objects that the annotations in
     * <code>parameterAnnotations</code> are associated with
     * @param parameterAnnotations An array of <code>AnnotationSetRefList</code> objects that contain the parameter
     * annotations for the methods in <code>parameterAnnotationMethods</code>
     */
    private AnnotationDirectoryItem(DexFile dexFile, AnnotationSetItem classAnnotations,
                                    FieldIdItem[] fieldAnnotationFields, AnnotationSetItem[] fieldAnnotations,
                                    MethodIdItem[] methodAnnotationMethods, AnnotationSetItem[] methodAnnotations,
                                    MethodIdItem[] parameterAnnotationMethods,
                                    AnnotationSetRefList[] parameterAnnotations) {
        super(dexFile);
        this.classAnnotations = classAnnotations;
        this.fieldAnnotationFields = fieldAnnotationFields;
        this.fieldAnnotations = fieldAnnotations;
        this.methodAnnotationMethods = methodAnnotationMethods;
        this.methodAnnotations = methodAnnotations;
        this.parameterAnnotationMethods = parameterAnnotationMethods;
        this.parameterAnnotations = parameterAnnotations;
    }

    /**
     * Returns an <code>AnnotationDirectoryItem</code> for the given values, and that has been interned into the given
     * <code>DexFile</code>
     * @param dexFile The <code>DexFile</code> that this item belongs to
     * @param classAnnotations The annotations associated with the class
     * @param fieldAnnotations A list of <code>FieldAnnotation</code> objects containing the field annotations
     * @param methodAnnotations A list of <code>MethodAnnotation</code> objects containing the method annotations
     * @param parameterAnnotations A list of <code>ParameterAnnotation</code> objects containin the parameter
     * annotations
     * @return an <code>AnnotationItem</code> for the given values, and that has been interned into the given
     * <code>DexFile</code>
     */
    public static AnnotationDirectoryItem getInternedAnnotationDirectoryItem(DexFile dexFile,
                                    AnnotationSetItem classAnnotations,
                                    List<FieldAnnotation> fieldAnnotations,
                                    List<MethodAnnotation> methodAnnotations,
                                    List<ParameterAnnotation> parameterAnnotations) {
        FieldIdItem[] fieldAnnotationFields = null;
        AnnotationSetItem[] fieldAnnotationsArray = null;
        MethodIdItem[] methodAnnotationMethods = null;
        AnnotationSetItem[] methodAnnotationsArray = null;
        MethodIdItem[] parameterAnnotationMethods = null;
        AnnotationSetRefList[] parameterAnnotationsArray = null;

        if (fieldAnnotations != null && fieldAnnotations.size() > 0) {
            fieldAnnotationFields = new FieldIdItem[fieldAnnotations.size()];
            fieldAnnotationsArray = new AnnotationSetItem[fieldAnnotations.size()];

            Collections.sort(fieldAnnotations);

            int index = 0;
            for (FieldAnnotation fieldAnnotation: fieldAnnotations) {
                fieldAnnotationFields[index] = fieldAnnotation.field;
                fieldAnnotationsArray[index++] = fieldAnnotation.annotationSet;
            }
        }

        if (methodAnnotations != null && methodAnnotations.size() > 0) {
            methodAnnotationMethods = new MethodIdItem[methodAnnotations.size()];
            methodAnnotationsArray = new AnnotationSetItem[methodAnnotations.size()];

            Collections.sort(methodAnnotations);

            int index = 0;
            for (MethodAnnotation methodAnnotation: methodAnnotations) {
                methodAnnotationMethods[index] = methodAnnotation.method;
                methodAnnotationsArray[index++] = methodAnnotation.annotationSet;
            }
        }

        if (parameterAnnotations != null && parameterAnnotations.size() > 0) {
            parameterAnnotationMethods = new MethodIdItem[parameterAnnotations.size()];
            parameterAnnotationsArray = new AnnotationSetRefList[parameterAnnotations.size()];

            Collections.sort(parameterAnnotations);

            int index = 0;
            for (ParameterAnnotation parameterAnnotation: parameterAnnotations) {
                parameterAnnotationMethods[index] = parameterAnnotation.method;
                parameterAnnotationsArray[index++] = parameterAnnotation.annotationSet;
            }
        }

        AnnotationDirectoryItem annotationDirectoryItem = new AnnotationDirectoryItem(dexFile, classAnnotations,
                fieldAnnotationFields, fieldAnnotationsArray, methodAnnotationMethods, methodAnnotationsArray,
                parameterAnnotationMethods, parameterAnnotationsArray);
        return dexFile.AnnotationDirectoriesSection.intern(annotationDirectoryItem);
    }

    /** {@inheritDoc} */
    protected void readItem(Input in, ReadContext readContext) {
        classAnnotations = (AnnotationSetItem)readContext.getOffsettedItemByOffset(ItemType.TYPE_ANNOTATION_SET_ITEM,
                in.readInt());
        fieldAnnotationFields = new FieldIdItem[in.readInt()];
        fieldAnnotations = new AnnotationSetItem[fieldAnnotationFields.length];

        methodAnnotationMethods = new MethodIdItem[in.readInt()];
        methodAnnotations = new AnnotationSetItem[methodAnnotationMethods.length];

        parameterAnnotationMethods = new MethodIdItem[in.readInt()];
        parameterAnnotations = new AnnotationSetRefList[parameterAnnotationMethods.length];

        for (int i=0; i<fieldAnnotations.length; i++) {
            fieldAnnotationFields[i] = dexFile.FieldIdsSection.getItemByIndex(in.readInt());
            fieldAnnotations[i] = (AnnotationSetItem)readContext.getOffsettedItemByOffset(
                    ItemType.TYPE_ANNOTATION_SET_ITEM, in.readInt());
        }

        for (int i=0; i<methodAnnotations.length; i++) {
            methodAnnotationMethods[i] = dexFile.MethodIdsSection.getItemByIndex(in.readInt());
            methodAnnotations[i] = (AnnotationSetItem)readContext.getOffsettedItemByOffset(
                    ItemType.TYPE_ANNOTATION_SET_ITEM, in.readInt());
        }

        for (int i=0; i<parameterAnnotations.length; i++) {
            parameterAnnotationMethods[i] = dexFile.MethodIdsSection.getItemByIndex(in.readInt());
            parameterAnnotations[i] = (AnnotationSetRefList)readContext.getOffsettedItemByOffset(
                    ItemType.TYPE_ANNOTATION_SET_REF_LIST, in.readInt());
        }
    }

    /** {@inheritDoc} */
    protected int placeItem(int offset) {
        if (!dexFile.getInplace()) {
            ArrayUtils.sortTwoArrays(fieldAnnotationFields, fieldAnnotations);
            ArrayUtils.sortTwoArrays(methodAnnotationMethods, methodAnnotations);
            ArrayUtils.sortTwoArrays(parameterAnnotationMethods, parameterAnnotations);
        }

        return offset + 16 + fieldAnnotations.length * 8 + methodAnnotations.length * 8 +
                parameterAnnotations.length * 8;
    }

    /** {@inheritDoc} */
    protected void writeItem(AnnotatedOutput out) {
        if (out.annotates()) {
            if (!isInternable() && parent != null) {
                out.annotate(0, parent.getClassType().getTypeDescriptor());
            }
            if (classAnnotations != null) {
                out.annotate(4, "class_annotations_off: 0x" + Integer.toHexString(classAnnotations.getOffset()));
            } else {
                out.annotate(4, "class_annotations_off:");
            }

            out.annotate(4, "annotated_fields_size: 0x" + Integer.toHexString(fieldAnnotations.length) + " (" +
                    fieldAnnotations.length + ")");
            out.annotate(4, "annotated_methods_size: 0x" + Integer.toHexString(methodAnnotations.length) + " (" +
                    methodAnnotations.length + ")");
            out.annotate(4, "annotated_parameters_size: 0x" + Integer.toHexString(parameterAnnotations.length) + " (" +
                    parameterAnnotations.length + ")");


            int index = 0;
            for (int i=0; i<fieldAnnotations.length; i++) {
                out.annotate(0, "[" + index++ + "] field_annotation");

                out.indent();
                out.annotate(4, "field: " + fieldAnnotationFields[i].getFieldName().getStringValue() + ":" +
                        fieldAnnotationFields[i].getFieldType().getTypeDescriptor());
                out.annotate(4, "annotations_off: 0x" + Integer.toHexString(fieldAnnotations[i].getOffset()));
                out.deindent();
            }

            index = 0;
            for (int i=0; i<methodAnnotations.length; i++) {
                out.annotate(0, "[" + index++ + "] method_annotation");
                out.indent();
                out.annotate(4, "method: " + methodAnnotationMethods[i].getMethodString());
                out.annotate(4, "annotations_off: 0x" + Integer.toHexString(methodAnnotations[i].getOffset()));
                out.deindent();
            }

            index = 0;
            for (int i=0; i<parameterAnnotations.length; i++) {
                out.annotate(0, "[" + index++ + "] parameter_annotation");
                out.indent();
                out.annotate(4, "method: " + parameterAnnotationMethods[i].getMethodString());
                out.annotate(4, "annotations_off: 0x" + Integer.toHexString(parameterAnnotations[i].getOffset()));
            }
        }

        out.writeInt(classAnnotations==null?0:classAnnotations.getOffset());
        out.writeInt(fieldAnnotations.length);
        out.writeInt(methodAnnotations.length);
        out.writeInt(parameterAnnotations.length);

        for (int i=0; i<fieldAnnotations.length; i++) {
            out.writeInt(fieldAnnotationFields[i].getIndex());
            out.writeInt(fieldAnnotations[i].getOffset());
        }

        for (int i=0; i<methodAnnotations.length; i++) {
            out.writeInt(methodAnnotationMethods[i].getIndex());
            out.writeInt(methodAnnotations[i].getOffset());
        }

        for (int i=0; i<parameterAnnotations.length; i++) {
            out.writeInt(parameterAnnotationMethods[i].getIndex());
            out.writeInt(parameterAnnotations[i].getOffset());
        }
    }

    /** {@inheritDoc} */public ItemType getItemType() {
        return ItemType.TYPE_ANNOTATIONS_DIRECTORY_ITEM;
    }

    /** {@inheritDoc} */
    public String getConciseIdentity() {
        return "annotation_directory_item @0x" + Integer.toHexString(getOffset());
    }

    /** {@inheritDoc} */
    public int compareTo(AnnotationDirectoryItem o) {
        if (!isInternable()) {
            if (!o.isInternable()) {
                return parent.compareTo(o.parent);
            }
            return -1;
        }

        if (!o.isInternable()) {
            return 1;
        }

        return classAnnotations.compareTo(o.classAnnotations);
    }

    /**
     * @return The annotations associated with the class
     */
    public AnnotationSetItem getClassAnnotations() {
        return classAnnotations;
    }

    /**
     * Iterates over the field annotations, calling delegate.processFieldAnnotations for each 
     * @param delegate the delegate to call
     */
    public void iterateFieldAnnotations(FieldAnnotationIteratorDelegate delegate) {
        for (int i=0; i<fieldAnnotationFields.length; i++) {
            delegate.processFieldAnnotations(fieldAnnotationFields[i], fieldAnnotations[i]);
        }
    }

    public static interface FieldAnnotationIteratorDelegate {
        void processFieldAnnotations(FieldIdItem field, AnnotationSetItem fieldAnnotations);
    }

    /**
     * Iterates over the method annotations, calling delegate.processMethodAnnotations for each
     * @param delegate the delegate to call
     */
    public void iterateMethodAnnotations(MethodAnnotationIteratorDelegate delegate) {
        for (int i=0; i<methodAnnotationMethods.length; i++) {
            delegate.processMethodAnnotations(methodAnnotationMethods[i], methodAnnotations[i]);
        }
    }

    public static interface MethodAnnotationIteratorDelegate {
        void processMethodAnnotations(MethodIdItem method, AnnotationSetItem methodAnnotations);
    }

    /**
     * Iterates over the parameter annotations, calling delegate.processParameterAnnotations for each 
     * @param delegate the delegate to call
     */
    public void iteratParameterAnnotations(ParameterAnnotationIteratorDelegate delegate) {
        for (int i=0; i<parameterAnnotationMethods.length; i++) {
            delegate.processParameterAnnotations(parameterAnnotationMethods[i], parameterAnnotations[i]);
        }
    }

    public static interface ParameterAnnotationIteratorDelegate {
        void processParameterAnnotations(MethodIdItem method, AnnotationSetRefList parameterAnnotations);
    }

    /**
     * @return true if this <code>AnnotationDirectoryItem</code> is internable. It is only internable if it has
     * only class annotations, but no field, method or parameter annotations
     */
    private boolean isInternable() {
        return classAnnotations != null &&
               fieldAnnotations.length == 0 &&
               methodAnnotations.length == 0 &&
               parameterAnnotations.length == 0;
    }

    /**
     * Sets the <code>ClassDefItem</code> that this <code>AnnotationDirectoryItem</code> is associated with.
     * This is only applicable if this AnnotationDirectoryItem contains only class annotations, and no field, method
     * or parameter annotations.
     * @param classDefItem the <code>ClassDefItem</code> that this <code>AnnotationDirectoryItem</code> is associated
     * with
     */
    protected void setParent(ClassDefItem classDefItem) {
        this.parent = classDefItem;
    }

    @Override
    public int hashCode() {
        //an instance is only internable if it has only class annotations, but
        //no other type of annotation
        if (!isInternable()) {
            return super.hashCode();
        }
        return classAnnotations.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) {
            return true;
        }
        if (o==null || !this.getClass().equals(o.getClass())) {
            return false;
        }

        AnnotationDirectoryItem other = (AnnotationDirectoryItem)o;
        return (this.compareTo(other) == 0);
    }

    public static class FieldAnnotation implements Comparable<FieldAnnotation> {
        public final FieldIdItem field;
        public final AnnotationSetItem annotationSet;

        public FieldAnnotation(FieldIdItem field, AnnotationSetItem annotationSet) {
            this.field = field;
            this.annotationSet = annotationSet;
        }

        public int compareTo(FieldAnnotation other) {
            return field.compareTo(other.field);
        }
    }

    public static class MethodAnnotation implements Comparable<MethodAnnotation> {
        public final MethodIdItem method;
        public final AnnotationSetItem annotationSet;

        public MethodAnnotation(MethodIdItem method, AnnotationSetItem annotationSet) {
            this.method = method;
            this.annotationSet = annotationSet;
        }

        public int compareTo(MethodAnnotation other) {
            return method.compareTo(other.method);
        }
    }

    public static class ParameterAnnotation implements Comparable<ParameterAnnotation> {
        public final MethodIdItem method;
        public final AnnotationSetRefList annotationSet;

        public ParameterAnnotation(MethodIdItem method, AnnotationSetRefList annotationSet) {
            this.method = method;
            this.annotationSet = annotationSet;
        }

        public int compareTo(ParameterAnnotation other) {
            return method.compareTo(other.method);
        }
    }
}
