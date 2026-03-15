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
    private static String configurationResourceName = "configuration.properties"

    /**
     * The main method
     *
     * @param   args    String[]    The command line arguments
     */
    static void main(String[] args) {
        /* Getting the version from the build.gradle file only works for jar deployments */

        def version = Main.class.package?.implementationVersion
        def configuration = getConfiguration()

        if (!configuration) {
            System.err.println("Failed to load configuration: Could not find ${configurationResourceName} on the classpath")
            System.exit(1)
        }

        int exitValue = run(configuration, version, args as List<String>)

        System.exit(exitValue)
    }

    /**
     * Gets the configuration
     *
     * @return  Configuration   The configuration or null if the configuration could not be loaded
     */
    private static getConfiguration() {
        def props = new Properties()
        def resourceStream = Main.class.getResourceAsStream('/' + configurationResourceName)

        if (!resourceStream) {
            resourceStream = Thread.currentThread().contextClassLoader?.getResourceAsStream(configurationResourceName)
        }

        if (resourceStream) {
            try {
                props.load(resourceStream)
            } finally {
                resourceStream.close()
            }

            def configuration = new Configuration()

            configuration.productsUrl = props.getProperty('app.productsUrl')
            configuration.solrUrl = props.getProperty('app.solrUrl')
            configuration.solrCollection = props.getProperty('app.solrCollection')

            if (!configuration.productsUrl || !configuration.solrUrl || !configuration.solrCollection) {
                return null
            }

            return configuration
        } else {
            return null
        }
    }

    /**
     * The run method
     *
     * @param   configuration   Configuration   The configuration
     * @param   version         String          The version of the application
     * @param   args            List<String>    The command line arguments
     * @return                  int             The exit code
     */
    private static int run(Configuration configuration, String version, List<String> args) {
        Runner runner = new Runner(configuration, version, args)

        return runner.run()
    }
}
