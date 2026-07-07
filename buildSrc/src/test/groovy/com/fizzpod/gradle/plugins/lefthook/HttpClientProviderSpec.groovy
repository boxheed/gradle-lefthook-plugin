/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import java.io.File
import java.io.IOException
import okhttp3.*
import spock.lang.Specification

class HttpClientProviderSpec extends Specification {

    private Call.Factory originalClient
    private Call.Factory mockClient
    private Call mockCall

    def setup() {
        originalClient = HttpClientProvider.getClient()
        mockClient = Mock(Call.Factory)
        mockCall = Mock(Call)
        HttpClientProvider.setClient(mockClient)
    }

    def cleanup() {
        HttpClientProvider.setClient(originalClient)
    }

    private Response createFakeResponse(int code, String bodyContent) {
        MediaType mediaType = MediaType.parse("text/plain")
        ResponseBody body = ResponseBody.create(bodyContent, mediaType)
        Request request = new Request.Builder().url("https://example.com").build()
        return new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(code == 200 ? "OK" : "Error")
            .body(body)
            .build()
    }

    def "downloadToFile writes stream content on successful response"() {
        setup:
            File tempFile = File.createTempFile("test-download", ".txt")
            tempFile.deleteOnExit()
            def response = createFakeResponse(200, "expected binary file contents")

        when:
            HttpClientProvider.downloadToFile("https://example.com/file.bin", tempFile)

        then:
            1 * mockClient.newCall(_ as Request) >> { Request req ->
                assert req.url().toString() == "https://example.com/file.bin"
                return mockCall
            }
            1 * mockCall.execute() >> response
            tempFile.text == "expected binary file contents"
    }

    def "downloadToFile creates parent directories if they do not exist"() {
        setup:
            File tempDir = File.createTempDir("test-dir-parent", "")
            tempDir.deleteOnExit()
            File nestedDir = new File(tempDir, "nested-subdir")
            File nestedFile = new File(nestedDir, "test-download.txt")
            def response = createFakeResponse(200, "hello nested world")

        when:
            HttpClientProvider.downloadToFile("https://example.com/file.bin", nestedFile)

        then:
            1 * mockClient.newCall(_ as Request) >> { Request req ->
                return mockCall
            }
            1 * mockCall.execute() >> response
            nestedDir.exists()
            nestedFile.text == "hello nested world"
    }

    def "downloadToFile throws IOException on non-successful response"() {
        setup:
            File tempFile = File.createTempFile("test-download", ".txt")
            tempFile.deleteOnExit()
            def response = createFakeResponse(404, "Not Found")

        when:
            HttpClientProvider.downloadToFile("https://example.com/file.bin", tempFile)

        then:
            1 * mockClient.newCall(_ as Request) >> { Request req ->
                return mockCall
            }
            1 * mockCall.execute() >> response
            thrown(IOException)
    }

    def "getClient returns default OkHttpClient instance when not overridden"() {
        setup:
            HttpClientProvider.setClient(null)
        
        when:
            def client = HttpClientProvider.getClient()
        
        then:
            client instanceof OkHttpClient
            ((OkHttpClient) client).connectTimeoutMillis() == 120000
            ((OkHttpClient) client).readTimeoutMillis() == 120000
            ((OkHttpClient) client).writeTimeoutMillis() == 120000
    }
}
