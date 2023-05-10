/*
 * Copyright 2016, Google LLC
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

package com.android.tools.smali.baksmali;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.raw.util.DexAnnotator;
import com.android.tools.smali.util.ConsoleUtil;
import com.android.tools.smali.util.jcommander.ExtendedParameters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.annotation.Nonnull;
import java.util.List;

@Parameters(commandDescription = "Prints an annotated hex dump for the given dex file")
@ExtendedParameters(
        commandName = "dump",
        commandAliases = "du")
public class DumpCommand extends DexInputCommand {

    @Parameter(names = {"-h", "-?", "--help"}, help = true,
            description = "Show usage information for this command.")
    private boolean help;

    public DumpCommand(@Nonnull List<JCommander> commandAncestors) {
        super(commandAncestors);
    }

    public void run() {
        if (help || inputList == null || inputList.isEmpty()) {
            usage();
            return;
        }

        if (inputList.size() > 1) {
            System.err.println("Too many files specified");
            usage();
            return;
        }

        String input = inputList.get(0);
        loadDexFile(input);

        try {
            dump(dexFile, System.out);
        } catch (IOException ex) {
            System.err.println("There was an error while dumping the dex file");
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Writes an annotated hex dump of the given dex file to output.
     *
     * @param dexFile The dex file to dump
     * @param output An OutputStream to write the annotated hex dump to. The caller is responsible for closing this
     *               when needed.
     *
     * @throws IOException
     */
    public static void dump(@Nonnull DexBackedDexFile dexFile, @Nonnull OutputStream output)
            throws IOException {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(output))) {
            int consoleWidth = ConsoleUtil.getConsoleWidth();
            if (consoleWidth <= 0) {
                consoleWidth = 120;
            }

            DexAnnotator annotator = new DexAnnotator(dexFile, consoleWidth);
            annotator.writeAnnotations(writer);
        }
    }
}
