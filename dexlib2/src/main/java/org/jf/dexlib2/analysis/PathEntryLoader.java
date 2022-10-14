/*
 * Copyright 2016, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * Neither the name of Google Inc. nor the names of its
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

package org.jf.dexlib2.analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.OatFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class PathEntryLoader {
    Opcodes opcodes;

    public Opcodes getOpcodes() {
        return opcodes;
    }

    final Set<File> loadedFiles = Sets.newHashSet();
    final List<ClassProvider> classProviders = Lists.newArrayList();

    public List<ClassProvider> getClassProviders() {
        return classProviders;
    }

    public PathEntryLoader(Opcodes opcodes) {
        this.opcodes = opcodes;
    }

    @Nonnull
    public List<ClassProvider> getResolvedClassProviders() {
        return classProviders;
    }

    public void loadEntry(@Nonnull File entryFile, boolean loadOatDependencies)
            throws IOException, NoDexException {
        if (loadedFiles.contains(entryFile)) {
            return;
        }

        MultiDexContainer<? extends DexBackedDexFile> container;
        try {
            container = DexFileFactory.loadDexContainer(entryFile, opcodes);
        } catch (DexFileFactory.UnsupportedFileTypeException ex) {
            throw new ClassPathResolver.ResolveException(ex);
        }

        List<String> entryNames = container.getDexEntryNames();

        if (entryNames.isEmpty()) {
            throw new NoDexException("%s contains no dex file", entryFile);
        }

        loadedFiles.add(entryFile);

        for (String entryName : entryNames) {
            classProviders.add(new DexClassProvider(container.getEntry(entryName).getDexFile()));
        }

        if (loadOatDependencies && container instanceof OatFile) {
            List<String> oatDependencies = ((OatFile) container).getBootClassPath();
            if (!oatDependencies.isEmpty()) {
                try {
                    loadOatDependencies(entryFile.getParentFile(), oatDependencies);
                } catch (ClassPathResolver.NotFoundException ex) {
                    throw new ClassPathResolver.ResolveException(ex, "Error while loading oat file %s", entryFile);
                } catch (NoDexException ex) {
                    throw new ClassPathResolver.ResolveException(ex, "Error while loading dependencies for oat file %s", entryFile);
                }
            }
        }
    }

    private void loadOatDependencies(@Nonnull File directory, @Nonnull List<String> oatDependencies)
            throws IOException, NoDexException, ClassPathResolver.NotFoundException {
        // We assume that all oat dependencies are located in the same directory as the oat file
        for (String oatDependency : oatDependencies) {
            String oatDependencyName = getFilenameForOatDependency(oatDependency);
            File file = new File(directory, oatDependencyName);
            if (!file.exists()) {
                throw new ClassPathResolver.NotFoundException("Cannot find dependency %s in %s", oatDependencyName, directory);
            }

            loadEntry(file, false);
        }
    }

    @Nonnull
    private String getFilenameForOatDependency(String oatDependency) {
        int index = oatDependency.lastIndexOf('/');

        String dependencyLeaf = oatDependency.substring(index + 1);
        if (dependencyLeaf.endsWith(".art")) {
            return dependencyLeaf.substring(0, dependencyLeaf.length() - 4) + ".oat";
        }
        return dependencyLeaf;
    }

    static class NoDexException extends Exception {
        public NoDexException(String message, Object... formatArgs) {
            super(String.format(message, formatArgs));
        }
    }
}