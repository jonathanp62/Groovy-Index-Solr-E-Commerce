package net.jmp.solr.index.ecommerce

/*
 * (#)Main.groovy   1.0.0   03/12/2026
 *
 * @author    Jonathan Parker
 * @version   1.0.0
 * @since     1.0.0
 *
 * MIT License
 *
 * Copyright (c) 2026 Jonathan M. Parker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * The main class for the Solr index e-commerce application
 */
class Main {
    /**
     * The main method
     *
     * @param   args    String[]    The command line arguments
     */
    static void main(String[] args) {
        /* Getting the version from the build.gradle file only works for jar deployments */

        def version = Main.class.package?.implementationVersion
        int exitValue = run(version, args as List<String>)

        System.exit(exitValue)
    }

    /**
     * The run method
     *
     * @param   version String          The version of the application
     * @param   args    List<String>    The command line arguments
     * @return          int             The exit code
     */
    private static int run(String version, List<String> args) {
        Runner runner = new Runner(version, args)

        return runner.run()
    }
}
