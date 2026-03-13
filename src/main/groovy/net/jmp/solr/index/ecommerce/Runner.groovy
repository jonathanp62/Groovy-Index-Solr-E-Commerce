package net.jmp.solr.index.ecommerce

/*
 * (#)Runner.groovy 1.0.0   03/12/2026
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

import groovy.json.JsonSlurper

import java.net.http.*

class Runner {
    /** The configuration */
    private Configuration configuration

    /** The version of the application */
    private String version

    /** The list of command line arguments */
    private List<String> args

    /**
     * The constructor
     *
     * @param configuration Configuration   The configuration
     * @param version       String          The version of the application
     * @param args          List<String>    The list of command line arguments
     */
    Runner(Configuration configuration,  version, List<String> args) {
        this.configuration = configuration
        this.version = version
        this.args = args
    }

    /**
     * The run method for the application
     *
     * @return int  The exit code
     */
    int run() {
        println("Solr Index E-Commerce Products ${this.version}")

        def productsBody = this.getProductsBody()

        if (productsBody) {
            def products = getProducts(productsBody)

            for (InboundProduct product : products) {
                println product
            }
        } else {
            System.err.println("Failed to get products")
            return 1
        }

        return 0
    }

    /**
     * Gets the products body
     *
     * @return  String  The body of the response or null if the request failed
     */
    private getProductsBody() {
        def client = HttpClient.newHttpClient()
        def request = HttpRequest.newBuilder()
                .uri(URI.create(this.configuration.productsUrl))
                .GET().build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            System.err.println("Failed to get products: ${response.statusCode()}")
            return null
        }

        return response.body()
    }

    /**
     * Gets the list of products from the JSON
     *
     * @param   json    String                  The JSON
     * @return          List<InboundProduct>    The list of products
     */
    private static List<InboundProduct> getProducts(String json) {
        def collection = new JsonSlurper().parseText(json)

        return collection as List<InboundProduct>
    }
}
