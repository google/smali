/*
 * Copyright 2013, Google LLC
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
 *     * Neither the name of Google LLC nor the names of its
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

package com.android.tools.smali.dexlib2.analysis.reflection;

import com.android.tools.smali.dexlib2.analysis.reflection.util.ReflectionUtils;
import com.android.tools.smali.dexlib2.iface.Annotation;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.Field;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.util.ChainedIterator;
import com.android.tools.smali.util.IteratorUtils;
import com.android.tools.smali.util.TransformedIterator;

import com.android.tools.smali.dexlib2.base.reference.BaseTypeReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;

/**
 * Wraps a ClassDef around a class loaded in the current VM
 *
 * Only supports the basic information exposed by ClassProto
 */
public class ReflectionClassDef extends BaseTypeReference implements ClassDef {
    private final Class cls;

    public ReflectionClassDef(Class cls) {
        this.cls = cls;
    }

    @Override public int getAccessFlags() {
        // the java modifiers appear to be the same as the dex access flags
        return cls.getModifiers();
    }

    @Nullable @Override public String getSuperclass() {
        if (Modifier.isInterface(cls.getModifiers())) {
            return "Ljava/lang/Object;";
        }
        Class superClass = cls.getSuperclass();
        if (superClass == null) {
            return null;
        }
        return ReflectionUtils.javaToDexName(superClass.getName());
    }

    @Nonnull @Override public List<String> getInterfaces() { 
        return Arrays.asList(cls.getInterfaces()).stream().map(
            i -> i == null ? null : ReflectionUtils.javaToDexName(i.getName()))
            .collect(Collectors.toList());
    }

    @Nullable @Override public String getSourceFile() {
        return null;
    }

    @Nonnull @Override public Set<? extends Annotation> getAnnotations() {
        return Collections.emptySet();
    }

    @Nonnull @Override public Iterable<? extends Field> getStaticFields() {
        return new Iterable<Field>() {
            @Nonnull @Override public Iterator<Field> iterator() {
                Iterator<java.lang.reflect.Field> staticFields = IteratorUtils.filter(
                        Arrays.asList(cls.getDeclaredFields()),
                        new Predicate<java.lang.reflect.Field>() {
                            @Override public boolean test(@Nullable java.lang.reflect.Field input) {
                                return input!=null && Modifier.isStatic(input.getModifiers());
                            }
                        });

                return new TransformedIterator<java.lang.reflect.Field, Field>(staticFields, new Function<java.lang.reflect.Field, Field>() {
                    @Nullable @Override public Field apply(@Nullable java.lang.reflect.Field input) {
                        return new ReflectionField(input);
                    }
                });
            }
        };
    }

    @Nonnull @Override public Iterable<? extends Field> getInstanceFields() {
        return new Iterable<Field>() {
            @Nonnull @Override public Iterator<Field> iterator() {
                Iterator<java.lang.reflect.Field> staticFields = IteratorUtils.filter(
                        Arrays.asList(cls.getDeclaredFields()),
                        new Predicate<java.lang.reflect.Field>() {
                            @Override public boolean test(@Nullable java.lang.reflect.Field input) {
                                return input!=null && !Modifier.isStatic(input.getModifiers());
                            }
                        });

                return new TransformedIterator<java.lang.reflect.Field, Field>(staticFields,
                        new Function<java.lang.reflect.Field, Field>() {
                            @Nullable @Override public Field apply(@Nullable java.lang.reflect.Field input) {
                                return new ReflectionField(input);
                            }
                        }
                );
            }
        };
    }

    @Nonnull @Override public Set<? extends Field> getFields() {
        return new AbstractSet<Field>() {
            @Nonnull @Override public Iterator<Field> iterator() {
                return new TransformedIterator<java.lang.reflect.Field, Field>(
                    Arrays.asList(cls.getDeclaredFields()).iterator(),
                        new Function<java.lang.reflect.Field, Field>() {
                            @Nullable @Override public Field apply(@Nullable java.lang.reflect.Field input) {
                                return new ReflectionField(input);
                            }
                        });
            }

            @Override public int size() {
                return cls.getDeclaredFields().length;
            }
        };
    }

    private static final int DIRECT_MODIFIERS = Modifier.PRIVATE | Modifier.STATIC;
    @Nonnull @Override public Iterable<? extends Method> getDirectMethods() {
        return new Iterable<Method>() {
            @Nonnull @Override public Iterator<Method> iterator() {
                Iterator<Method> constructorIterator =
                        new TransformedIterator<Constructor, Method>(
                            Arrays.asList(cls.getDeclaredConstructors()).iterator(),
                                new Function<Constructor, Method>() {
                                    @Nullable @Override public Method apply(@Nullable Constructor input) {
                                        return new ReflectionConstructor(input);
                                    }
                                });

                Iterator<java.lang.reflect.Method> directMethods = IteratorUtils.filter(
                        Arrays.asList(cls.getDeclaredMethods()),
                        new Predicate<java.lang.reflect.Method>() {
                            @Override public boolean test(@Nullable java.lang.reflect.Method input) {
                                return input != null && (input.getModifiers() & DIRECT_MODIFIERS) != 0;
                            }
                        });

                Iterator<Method> methodIterator = new TransformedIterator<java.lang.reflect.Method, Method>(
                    directMethods,
                        new Function<java.lang.reflect.Method, Method>() {
                            @Nullable @Override public Method apply(@Nullable java.lang.reflect.Method input) {
                                return new ReflectionMethod(input);
                            }
                        });
                return new ChainedIterator<Method>(constructorIterator, methodIterator);
            }
        };
    }

    @Nonnull @Override public Iterable<? extends Method> getVirtualMethods() {
        return new Iterable<Method>() {
            @Nonnull @Override public Iterator<Method> iterator() {
                Iterator<java.lang.reflect.Method> directMethods = IteratorUtils.filter(
                        Arrays.asList(cls.getDeclaredMethods()),
                        new Predicate<java.lang.reflect.Method>() {
                            @Override public boolean test(@Nullable java.lang.reflect.Method input) {
                                return input != null && (input.getModifiers() & DIRECT_MODIFIERS) == 0;
                            }
                        });

                return new TransformedIterator<java.lang.reflect.Method, Method>(directMethods,
                        new Function<java.lang.reflect.Method, Method>() {
                            @Nullable @Override public Method apply(@Nullable java.lang.reflect.Method input) {
                                return new ReflectionMethod(input);
                            }
                        });
            }
        };
    }

    @Nonnull @Override public Set<? extends Method> getMethods() {
        return new AbstractSet<Method>() {
            @Nonnull @Override public Iterator<Method> iterator() {
                Iterator<Method> constructorIterator =
                        new TransformedIterator<Constructor, Method>(
                            Arrays.asList(cls.getDeclaredConstructors()).iterator(),
                                new Function<Constructor, Method>() {
                                    @Nullable @Override public Method apply(@Nullable Constructor input) {
                                        return new ReflectionConstructor(input);
                                    }
                                });

                Iterator<Method> methodIterator =
                        new TransformedIterator<java.lang.reflect.Method, Method>(
                            Arrays.asList(cls.getDeclaredMethods()).iterator(),
                                new Function<java.lang.reflect.Method, Method>() {
                                    @Nullable @Override public Method apply(@Nullable java.lang.reflect.Method input) {
                                        return new ReflectionMethod(input);
                                    }
                                });
                return new ChainedIterator<Method>(constructorIterator, methodIterator);
            }

            @Override public int size() {
                return cls.getDeclaredMethods().length + cls.getDeclaredConstructors().length;
            }
        };
    }

    @Nonnull @Override public String getType() {
        return ReflectionUtils.javaToDexName(cls.getName());
    }
}
