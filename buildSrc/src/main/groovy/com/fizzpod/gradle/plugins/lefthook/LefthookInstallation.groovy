/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import groovy.json.*
import javax.inject.Inject
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import groovy.transform.Memoized

public class LefthookInstallation {

    static File install(String repo, OS.Arch arch, OS.Family os, String version, File location) {
        def params = [
            arch: arch,
            os: os,
            location: location,
            version: version,
            repo: repo
        ]
        def context = [params: params]
        def result = Optional.ofNullable(context)
            .map(x -> LefthookInstallation.os(x))
            .map(x -> LefthookInstallation.arch(x))
            .map(x -> LefthookInstallation.artifact(x))
            .map(x -> LefthookInstallation.bin(x))
            .map(x -> LefthookInstallation.download(x))
            .map(x -> x.binary)
            .orElseThrow(() -> new RuntimeException("Unable to download lefthook"))
        return result
    }

    static File findBinary(File location, OS.Family os, OS.Arch arch) {
        def binaryPattern = LefthookInstallation.getBinaryName("v?(\\d+\\.\\d+\\.\\d+)", os, arch) + ".*"
        def binary = null
        if(location.exists()) {
            location.listFiles().each { File file ->
                if (file.name =~ binaryPattern) {
                    if(binary == null || binary.lastModified() < file.lastModified()) {
                        binary = file
                    }
                }
            }
        }
        return binary
    }

    static Map download(Map x) {
        Loggy.debug("{} Entry : {}", "LefthookInstallation", x)
        try {
            if(!x.binary.exists()) {
                LefthookInstallation.downloadAndInstall(x.url, x.binary, x.os)
            } else {
                FileUtils.touch(x.binary)
            }
            def result = x.binary.exists()? x: null
            Loggy.debug("{} Exit : {}", "LefthookInstallation", result != null? result: "null")
            return result
        } catch (Exception e) {
            throw e
        }
    }

    static File downloadAndInstall(String url, File binary, OS.Family os) {
        def tmp = new File(binary.getParentFile(), binary.getName())
        FileUtils.copyURLToFile(new URL(url), tmp, 120000, 120000)
        binary.setExecutable(true)
        return binary
    }

    static Map bin(Map x) {
        Loggy.debug("{} Entry : {}", "LefthookInstallation", x)
        def location = x.params.location
        def version = x.version
        def os = x.os
        def arch = x.arch
        x.binary = LefthookInstallation.binary(location, version, os, arch)
        def result = x.binary? x: null
        Loggy.debug("{} Exit : {}", "LefthookInstallation", result != null? result: "null")
        return result
    }

    @Memoized
    static File binary(File location, String version, OS.Family os, OS.Arch arch) {
        def name = LefthookInstallation.getBinaryName(version, os, arch)
        return new File(location, name)
    }

    @Memoized
    static String getBinaryName(String version, OS.Family os, OS.Arch arch) {
        def osId = os.id
        def archId = arch.id
        def extension = os == OS.Family.WINDOWS? ".exe": ""
        def name = "lefthook_${version}_${osId}_${archId}${extension}"
        return name
    }

    static Map artifact(Map x) {
        Loggy.debug("{} Entry : {}", "LefthookInstallation", x)
        x = x + LefthookInstallation.resolveArtifact(x.params.repo, x.arch, x.os, x.params.version)
        Loggy.debug("{} Exit : {}", "LefthookInstallation", x != null? x: "null")
        return x
    }

    @Memoized
    static Map resolveArtifact(String repo, OS.Arch arch, OS.Family os, String version) {
        def artifact = GitHubClient.resolve(repo, arch, os, version)
        return [url: artifact.url, version: artifact.version]
    }

    // Memoize applied to the method
    @Memoized
    static Map os(Map x) {
        Loggy.debug("{} Entry : {}", "LefthookInstallation", x)
        x.os = x.params.os
        def result = x.os? x: null
        Loggy.debug("{} Exit : {}", "LefthookInstallation", result != null? result: "null")
        return result
    }

    @Memoized
    static Map arch(Map x) {
        Loggy.debug("{} Entry : {}", "LefthookInstallation", x)
        x.arch = x.params.arch
        def result = x.arch? x: null
        Loggy.debug("{} Exit : {}", "LefthookInstallation", result != null? result: "null")
        return result
    }

}
