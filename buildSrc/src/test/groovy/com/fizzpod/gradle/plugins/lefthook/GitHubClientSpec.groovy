/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import okhttp3.*
import spock.lang.Specification

class GitHubClientSpec extends Specification {

    private Call.Factory originalClient
    private Call.Factory mockClient
    private Call mockCall
    private Request capturedRequest

    def setup() {
        originalClient = HttpClientProvider.getClient()
        mockClient = Mock(Call.Factory)
        mockCall = Mock(Call)

        HttpClientProvider.setClient(mockClient)
    }

    def cleanup() {
        HttpClientProvider.setClient(originalClient)
    }

    private Response createFakeResponse(int code, String jsonContent) {
        MediaType mediaType = MediaType.parse("application/json")
        ResponseBody body = ResponseBody.create(jsonContent, mediaType)
        Request request = new Request.Builder().url("https://api.github.com").build()
        return new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(code == 200 ? "OK" : "Error")
            .body(body)
            .build()
    }

    def "resolve with valid token adds authorization header"() {
        setup:
            String token = "ghp_valid_token_1234567890abcdef"
            def responseJson = '''{
                "tag_name": "v1.0.0",
                "assets": [
                    {
                        "name": "lefthook_1.0.0_Linux_x86_64",
                        "browser_download_url": "https://github.com/downloads/lefthook_Linux"
                    }
                ]
            }'''
            def response = createFakeResponse(200, responseJson)
            
        when:
            def result = GitHubClient.resolve("evilmartians/lefthook", OS.Arch.AMD64, OS.Family.LINUX, "v1.0.0", token)

        then:
            1 * mockClient.newCall(_ as Request) >> { Request req ->
                capturedRequest = req
                return mockCall
            }
            1 * mockCall.execute() >> response
            
            capturedRequest != null
            capturedRequest.header("Authorization") == "Bearer ghp_valid_token_1234567890abcdef"
            result.url == "https://github.com/downloads/lefthook_Linux"
            result.version == "v1.0.0"
    }

    def "resolve with short token does not add authorization header"() {
        setup:
            String token = "abc" // length < 4
            def responseJson = '''{
                "tag_name": "v2.0.0",
                "assets": [
                    {
                        "name": "lefthook_2.0.0_Linux_x86_64",
                        "browser_download_url": "https://github.com/downloads/lefthook_Linux"
                    }
                ]
            }'''
            def response = createFakeResponse(200, responseJson)
            
        when:
            GitHubClient.resolve("evilmartians/lefthook", OS.Arch.AMD64, OS.Family.LINUX, "v2.0.0", token)

        then:
            1 * mockClient.newCall(_ as Request) >> { Request req ->
                capturedRequest = req
                return mockCall
            }
            1 * mockCall.execute() >> response
            
            capturedRequest != null
            capturedRequest.header("Authorization") == null
    }

    def "resolve with null, empty, or whitespace token does not add authorization header"() {
        setup:
            def responseJson = '''{
                "tag_name": "v3.0.0",
                "assets": [
                    {
                        "name": "lefthook_3.0.0_Linux_x86_64",
                        "browser_download_url": "https://github.com/downloads/lefthook_Linux"
                    }
                ]
            }'''
            def response = createFakeResponse(200, responseJson)

        when:
            GitHubClient.resolve("evilmartians/lefthook", OS.Arch.AMD64, OS.Family.LINUX, "v3.0.0", tokenVal)

        then:
            1 * mockClient.newCall(_ as Request) >> { Request req ->
                capturedRequest = req
                return mockCall
            }
            1 * mockCall.execute() >> response
            
            capturedRequest != null
            capturedRequest.header("Authorization") == null

        where:
            tokenVal << [null, "", "   ", "\n"]
    }

    def "resolve propagates API errors correctly"() {
        setup:
            String token = "ghp_error_token"
            def response = createFakeResponse(401, '{"message": "Bad credentials"}')

        when:
            GitHubClient.resolve("evilmartians/lefthook", OS.Arch.AMD64, OS.Family.LINUX, "v4.0.0", token)

        then:
            1 * mockClient.newCall(_ as Request) >> { Request req ->
                capturedRequest = req
                return mockCall
            }
            1 * mockCall.execute() >> response
            
            def e = thrown(Exception)
            e instanceof IOException || e.cause instanceof IOException
    }
}
