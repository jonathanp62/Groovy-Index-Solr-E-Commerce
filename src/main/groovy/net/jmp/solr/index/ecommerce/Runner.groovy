package net.jmp.solr.index.ecommerce

/*
 * (#)Runner.groovy 1.1.1   03/17/2026
 * (#)Runner.groovy 1.0.1   03/16/2026
 * (#)Runner.groovy 1.0.0   03/12/2026
 *
 * @author    Jonathan Parker
 * @version   1.1.1
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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.net.http.*

class Runner {
    /** The configuration */
    private Configuration configuration

    /** The version of the application */
    private String version

    /** The list of command line arguments */
    private List<String> args

    /** The HTTP client */
    private HttpClient client = HttpClient.newHttpClient()

    /**
     * The constructor
     *
     * @param configuration Configuration   The configuration
     * @param version       String          The version of the application
     * @param args          List<String>    The list of command line arguments
     */
    Runner(Configuration configuration,  String version, List<String> args) {
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

        if (!productsBody) {
            System.err.println("Failed to get products")
            return 1
        }

        def inboundProducts = getInboundProducts(productsBody)
        def outboundProducts = getOutboundProducts(inboundProducts)

        def payloads = outboundProducts.collect { op ->
            [
                    "id": op.id,
                    "product_id": op.productId,
                    "title": op.title,
                    "price": op.price,
                    "description": op.description,
                    "category": op.category,
                    "image": op.image,
                    "rating_rate": op.ratingRate,
                    "rating_count": op.ratingCount
            ]
        }

        def jsonPayload = JsonOutput.toJson(payloads)

        if (this.saveProducts(jsonPayload) != 200) {
            return 1
        }

        if (this.commitProducts() != 200) {
            return 1
        }

        return 0
    }

    /**
     * Gets the products body
     *
     * @return  String  The body of the response or null if the request failed
     */
    private String getProductsBody() {
        def request = HttpRequest.newBuilder()
                .uri(URI.create(this.configuration.productsUrl))
                .GET()
                .build()

        try {
            def response = this.client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                System.err.println("Failed to get products: ${response.statusCode()}")
                return null
            }

            return response.body()
        } catch (Exception e) {
            System.err.println("Exception getting products: ${e.message}")
            return null
        }
    }

    /**
     * Gets the list of inbound products from the JSON
     *
     * @param   json    String                  The JSON
     * @return          List<InboundProduct>    The list of products
     */
    private static List<InboundProduct> getInboundProducts(String json) {
        def collection

        try {
            collection = new JsonSlurper().parseText(json)
        } catch (Exception e) {
            System.err.println("Exception parsing products JSON: ${e.message}")
            return []
        }

        if (!(collection instanceof List)) {
            System.err.println("Unexpected products JSON format: ${collection?.getClass()?.name}")
            return []
        }

        return ((List) collection).collect { Object item ->
            if (!(item instanceof Map)) {
                return null
            }

            Map productMap = (Map) item

            def inboundProduct = new InboundProduct()

            inboundProduct.id = (productMap.id ?: 0) as int
            inboundProduct.title = productMap.title as String
            inboundProduct.price = (productMap.price ?: 0.0d) as double
            inboundProduct.description = productMap.description as String
            inboundProduct.category = productMap.category as String
            inboundProduct.image = productMap.image as String

            def ratingObj = productMap.rating

            if (ratingObj instanceof Map) {
                Map ratingMap = (Map) ratingObj

                def inboundRating = new InboundRating()

                inboundRating.rate = (ratingMap.rate ?: 0.0d) as double
                inboundRating.count = (ratingMap.count ?: 0) as int

                inboundProduct.rating = inboundRating
            }

            return inboundProduct
        }.findAll { it != null }    // 'it' is each collection item, equivalent to { InboundProduct p -> p != null }
    }

    /**
     * Gets the list of outbound products from the inbound products
     *
     * @param   json    List<InboundProduct>    The list of inbound products
     * @return          List<OutboundProduct>   The list of outbound products
     */
    private static List<OutboundProduct> getOutboundProducts(List<InboundProduct> inboundProducts) {
        def outboundProducts = []

        for (InboundProduct inboundProduct : inboundProducts) {
            def outboundProduct = new OutboundProduct()

            outboundProduct.id = "Product-" + inboundProduct.id
            outboundProduct.productId = inboundProduct.id
            outboundProduct.title = inboundProduct.title
            outboundProduct.price = inboundProduct.price
            outboundProduct.description = inboundProduct.description
            outboundProduct.category = inboundProduct.category
            outboundProduct.image = inboundProduct.image

            if (inboundProduct.rating) {
                outboundProduct.ratingRate = inboundProduct.rating.rate
                outboundProduct.ratingCount = inboundProduct.rating.count
            }

            outboundProducts.add(outboundProduct)
        }

        return outboundProducts
    }

    /**
     * Saves the products to Solr
     *
     * @param   json    String  The JSON
     */
    private int saveProducts(String json) {
        def request = HttpRequest.newBuilder()
                .uri(URI.create(this.configuration.solrUrl + "/" + this.configuration.solrCollection + "/update"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build()

        try {
            def response = this.client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                System.err.println("Failed to save products: ${response.statusCode()}")
                System.err.println("Response body: ${response.body()}")
            }

            return response.statusCode()
        } catch (Exception e) {
            System.err.println("Exception saving products: ${e.message}")
            return 500
        }
    }

    /**
     * Commits the products
     *
     * @return  int The response status code
     */
    private int commitProducts() {
        def request = HttpRequest.newBuilder()
                .uri(URI.create(this.configuration.solrUrl + "/" + this.configuration.solrCollection + "/update?commit=true"))
                .GET()
                .build()

        try {
            def response = this.client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                System.err.println("Failed to commit products: ${response.statusCode()}")
                System.err.println("Response body: ${response.body()}")
            }

            return response.statusCode()
        } catch (Exception e) {
            System.err.println("Exception committing products: ${e.message}")
            return 500
        }
    }
}
