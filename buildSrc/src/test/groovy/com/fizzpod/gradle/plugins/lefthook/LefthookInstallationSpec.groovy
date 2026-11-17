/* (C) 2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import okhttp3.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LefthookInstallationSpec extends Specification {

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

    private Response createBinaryResponse(int code, byte[] bytes) {
        MediaType mediaType = MediaType.parse("application/octet-stream")
        ResponseBody body = ResponseBody.create(bytes, mediaType)
        Request request = new Request.Builder().url("https://example.com/lefthook").build()
        return new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(code == 200 ? "OK" : "Error")
            .body(body)
            .build()
    }

    private byte[] gzipBytes(byte[] raw) {
        def baos = new ByteArrayOutputStream()
        new GZIPOutputStream(baos).withStream { gz ->
            gz.write(raw)
        }
        return baos.toByteArray()
    }

    private byte[] zipBytes(String entryName, byte[] raw) {
        def baos = new ByteArrayOutputStream()
        new ZipOutputStream(baos).withStream { zos ->
            zos.putNextEntry(new ZipEntry(entryName))
            zos.write(raw)
            zos.closeEntry()
        }
        return baos.toByteArray()
    }

    def "downloadAndInstall saves raw binary and marks it executable"() {
        setup:
            def binary = new File(temporaryFolder.root, "lefthook_v1.0.0_Linux_x86_64")
            byte[] payload = "raw-binary-content".bytes
            1 * mockClient.newCall(_ as Request) >> mockCall
            1 * mockCall.execute() >> createBinaryResponse(200, payload)

        when:
            def result = LefthookInstallation.downloadAndInstall("https://example.com/lefthook_1.0.0_Linux_x86_64", binary, OS.Family.LINUX)

        then:
            result == binary
            binary.exists()
            binary.bytes == payload
            binary.canExecute()
    }

    def "downloadAndInstall decompresses .gz archive directly to binary"() {
        setup:
            def binary = new File(temporaryFolder.root, "lefthook_v1.0.0_Linux_x86_64")
            byte[] raw = "gzipped-binary-content".bytes
            1 * mockClient.newCall(_ as Request) >> mockCall
            1 * mockCall.execute() >> createBinaryResponse(200, gzipBytes(raw))

        when:
            def result = LefthookInstallation.downloadAndInstall("https://example.com/lefthook_1.0.0_Linux_x86_64.gz", binary, OS.Family.LINUX)

        then:
            result == binary
            binary.exists()
            binary.bytes == raw
            binary.canExecute()
    }

    def "downloadAndInstall extracts .zip archive and renames lefthook.exe to binary"() {
        setup:
            def binary = new File(temporaryFolder.root, "lefthook_v1.0.0_Windows_x86_64.exe")
            byte[] raw = "windows-binary-content".bytes
            1 * mockClient.newCall(_ as Request) >> mockCall
            1 * mockCall.execute() >> createBinaryResponse(200, zipBytes("lefthook.exe", raw))

        when:
            def result = LefthookInstallation.downloadAndInstall("https://example.com/lefthook_1.0.0_Windows_x86_64.zip", binary, OS.Family.WINDOWS)

        then:
            result == binary
            binary.exists()
            binary.bytes == raw
            binary.canExecute()
    }

    def "downloadAndInstall throws IOException when HTTP download fails"() {
        setup:
            def binary = new File(temporaryFolder.root, "lefthook_v1.0.0_Linux_x86_64")
            1 * mockClient.newCall(_ as Request) >> mockCall
            1 * mockCall.execute() >> createBinaryResponse(404, "Not Found".bytes)

        when:
            LefthookInstallation.downloadAndInstall("https://example.com/lefthook_1.0.0_Linux_x86_64.gz", binary, OS.Family.LINUX)

        then:
            thrown(IOException)
            !binary.exists()
    }

    def "downloadAndInstall throws IOException on corrupt .gz payload"() {
        setup:
            def binary = new File(temporaryFolder.root, "lefthook_v1.0.0_Linux_x86_64")
            1 * mockClient.newCall(_ as Request) >> mockCall
            1 * mockCall.execute() >> createBinaryResponse(200, "not-a-gzip-stream".bytes)

        when:
            LefthookInstallation.downloadAndInstall("https://example.com/lefthook_1.0.0_Linux_x86_64.gz", binary, OS.Family.LINUX)

        then:
            thrown(IOException)
    }
}
