/* (C) 2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import okhttp3.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LefthookScriptInstallerSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

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

    private Response createScriptResponse(int code, String bodyText) {
        MediaType mediaType = MediaType.parse("text/plain")
        ResponseBody body = ResponseBody.create(bodyText, mediaType)
        Request request = new Request.Builder().url("https://example.com/commit-msg").build()
        return new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(code == 200 ? "OK" : "Error")
            .body(body)
            .build()
    }

    def "install downloads http script to hook directory and marks executable"() {
        setup:
            def installer = new LefthookScriptInstaller(temporaryFolder.root, ["commit-msg"])
            1 * mockClient.newCall(_ as Request) >> mockCall
            1 * mockCall.execute() >> createScriptResponse(200, "#!/bin/sh\necho ok\n")

        when:
            def hookName = installer.install("https://example.com/hooks/commit-msg")

        then:
            hookName == "commit-msg"
            def scriptFile = new File(temporaryFolder.root, "commit-msg/commit-msg")
            scriptFile.exists()
            scriptFile.text == "#!/bin/sh\necho ok\n"
            scriptFile.canExecute()
    }

    def "install extracts classpath resource to hook directory"() {
        setup:
            def installer = new LefthookScriptInstaller(temporaryFolder.root, ["pre-commit"])

        when:
            def hookName = installer.install("classpath:/.gitkeep")

        then:
            hookName == ".gitkeep"
            def scriptFile = new File(temporaryFolder.root, "pre-commit/.gitkeep")
            scriptFile.exists()
            scriptFile.canExecute()
    }

    def "install throws RuntimeException when location is null"() {
        setup:
            def installer = new LefthookScriptInstaller(null, ["pre-commit"])

        when:
            installer.install("https://example.com/hooks/pre-commit")

        then:
            def e = thrown(RuntimeException)
            e.message == "Lefthook location is not set"
    }

    def "doInstall throws RuntimeException with resource name when location is missing in context"() {
        when:
            LefthookScriptInstaller.doInstall([location: null, resource: "https://example.com/hooks/pre-commit", stack: ["pre-commit"]])

        then:
            def e = thrown(RuntimeException)
            e.message == "Unable to install https://example.com/hooks/pre-commit"
    }

    def "install throws FileNotFoundException for missing classpath resource"() {
        setup:
            def installer = new LefthookScriptInstaller(temporaryFolder.root, ["pre-commit"])

        when:
            installer.install("classpath:/does-not-exist.sh")

        then:
            def e = thrown(Exception)
            (e instanceof FileNotFoundException ? e : e.cause) instanceof FileNotFoundException
    }

    def "install throws IOException when http download fails"() {
        setup:
            def installer = new LefthookScriptInstaller(temporaryFolder.root, ["commit-msg"])
            1 * mockClient.newCall(_ as Request) >> mockCall
            1 * mockCall.execute() >> createScriptResponse(500, "Server Error")

        when:
            installer.install("https://example.com/hooks/commit-msg")

        then:
            def e = thrown(Exception)
            (e instanceof IOException ? e : e.cause) instanceof IOException
    }
}
