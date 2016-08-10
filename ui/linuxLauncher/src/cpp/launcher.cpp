/*
 * Copyright (c) 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
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


#include <dlfcn.h>
#include <locale.h>
#include <string>
#include <libgen.h>
#include <stdio.h>
#include <unistd.h>


typedef bool (*start_launcher)(int argc, char* argv[]);
typedef void (*stop_launcher)();

#define MAX_PATH 1024

std::string GetProgramPath() {
    std::string result;
    char *buffer = new char[MAX_PATH];

    if (buffer != NULL) {
        if (readlink("/proc/self/exe", buffer, MAX_PATH - 1) != -1) {
            buffer[MAX_PATH - 1] = '\0';
            result = buffer;
        }

        delete[] buffer;
    }

    return result;
}

int main(int argc, char *argv[]) {
    int result = 1;
    setlocale(LC_ALL, "en_US.utf8");
    void* library = NULL;

    {
        std::string programPath = GetProgramPath();
        std::string libraryName = dirname((char*)programPath.c_str());
        libraryName += "/libpackager.so";
        library = dlopen(libraryName.c_str(), RTLD_LAZY);

        if (library == NULL) {
            printf("%s not found.\n", libraryName.c_str());
        }
    }

    if (library != NULL) {
        start_launcher start = (start_launcher)dlsym(library, "start_launcher");
        stop_launcher stop = (stop_launcher)dlsym(library, "stop_launcher");

        if (start(argc, argv) == true) {
            result = 0;

            if (stop != NULL) {
                stop();
            }
        }

        dlclose(library);
    }


    return result;
}
