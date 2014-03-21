/*
 * Copyright 2014, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.smalidea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.PsiModifier.ModifierConstant;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.psi.SmaliElementTypes;
import org.jf.smalidea.psi.stub.SmaliClassStub;

import java.util.Collection;
import java.util.List;

public class SmaliClass extends SmaliStubBasedPsiElement<SmaliClassStub> implements PsiClass {
    public SmaliClass(@NotNull SmaliClassStub stub) {
        super(stub, SmaliElementTypes.CLASS);
    }

    public SmaliClass(@NotNull ASTNode node) {
        super(node);
    }

    @Override public boolean hasTypeParameters() {
        // TODO: implement generics
        return false;
    }

    @Nullable @Override public String getQualifiedName() {
        SmaliClassStatement classStatement = findChildByClass(SmaliClassStatement.class);
        if (classStatement == null) {
            return null;
        }
        return classStatement.getJavaType();
    }

    @NotNull public String getPackageName() {
        String name = getQualifiedName();
        if (name == null) {
            return "";
        }
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return name.substring(0, lastDot);
    }

    @Override public boolean isInterface() {
        return false;
    }

    @Override public boolean isAnnotationType() {
        return false;
    }

    @Override public boolean isEnum() {
        return false;
    }

    @Nullable @Override public PsiReferenceList getExtendsList() {
        return null;
    }

    @Nullable @Override public PsiReferenceList getImplementsList() {
        return null;
    }

    @NotNull @Override public PsiClassType[] getExtendsListTypes() {
        return new PsiClassType[0];
    }

    @NotNull @Override public PsiClassType[] getImplementsListTypes() {
        return new PsiClassType[0];
    }

    @Nullable @Override public PsiClass getSuperClass() {
        return null;
    }

    @Override public PsiClass[] getInterfaces() {
        return new PsiClass[0];
    }

    @NotNull @Override public PsiClass[] getSupers() {
        return new PsiClass[0];
    }

    @NotNull @Override public PsiClassType[] getSuperTypes() {
        return new PsiClassType[0];
    }

    @NotNull @Override public PsiField[] getFields() {
        return new PsiField[0];
    }

    @NotNull @Override public PsiMethod[] getMethods() {
        return new PsiMethod[0];
    }

    @NotNull @Override public PsiMethod[] getConstructors() {
        return new PsiMethod[0];
    }

    @NotNull @Override public PsiClass[] getInnerClasses() {
        return new PsiClass[0];
    }

    @NotNull @Override public PsiClassInitializer[] getInitializers() {
        return new PsiClassInitializer[0];
    }

    @NotNull @Override public PsiField[] getAllFields() {
        return new PsiField[0];
    }

    @NotNull @Override public PsiMethod[] getAllMethods() {
        return new PsiMethod[0];
    }

    @NotNull @Override public PsiClass[] getAllInnerClasses() {
        return new PsiClass[0];
    }

    @Nullable @Override public PsiField findFieldByName(@NonNls String name, boolean checkBases) {
        return null;
    }

    @Nullable @Override public PsiMethod findMethodBySignature(PsiMethod patternMethod, boolean checkBases) {
        return null;
    }

    @NotNull @Override public PsiMethod[] findMethodsBySignature(PsiMethod patternMethod, boolean checkBases) {
        return new PsiMethod[0];
    }

    @NotNull @Override public PsiMethod[] findMethodsByName(@NonNls String name, boolean checkBases) {
        return new PsiMethod[0];
    }

    @NotNull @Override
    public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(@NonNls String name, boolean checkBases) {
        return null;
    }

    @NotNull @Override public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors() {
        return null;
    }

    @Nullable @Override public PsiClass findInnerClassByName(@NonNls String name, boolean checkBases) {
        return null;
    }

    @Nullable @Override public PsiElement getLBrace() {
        return null;
    }

    @Nullable @Override public PsiElement getRBrace() {
        return null;
    }

    @Nullable @Override public PsiIdentifier getNameIdentifier() {
        return null;
    }

    @Override public PsiElement getScope() {
        return null;
    }

    @Override public boolean isInheritor(@NotNull PsiClass baseClass, boolean checkDeep) {
        return false;
    }

    @Override public boolean isInheritorDeep(PsiClass baseClass, @Nullable PsiClass classToByPass) {
        return false;
    }

    @Nullable @Override public PsiClass getContainingClass() {
        return null;
    }

    @NotNull @Override public Collection<HierarchicalMethodSignature> getVisibleSignatures() {
        return null;
    }

    @Override public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null;
    }

    @Nullable @Override public PsiDocComment getDocComment() {
        return null;
    }

    @Override public boolean isDeprecated() {
        return false;
    }

    @Nullable @Override public PsiTypeParameterList getTypeParameterList() {
        return null;
    }

    @NotNull @Override public PsiTypeParameter[] getTypeParameters() {
        return new PsiTypeParameter[0];
    }

    @Nullable @Override public PsiModifierList getModifierList() {
        return null;
    }

    @Override public boolean hasModifierProperty(@ModifierConstant @NonNls @NotNull String name) {
        return false;
    }
}