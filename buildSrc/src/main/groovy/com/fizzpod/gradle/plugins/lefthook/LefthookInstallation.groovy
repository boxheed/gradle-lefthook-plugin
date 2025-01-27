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

public class LefthookInstallation {

    public static final String LEFTHOOK_INSTALL_DIR = ".lefthook"

    static def install = { String repo, String arch, String os, String version, File location ->
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

    static def download = Loggy.wrap({ x ->
        if(!x.binary.exists()) {
            LefthookInstallation.downloadAndInstall(x.url, x.binary, x.os)
        } else {
            FileUtils.touch(x.binary)
        }
        return x.binary.exists()? x: null
    })

    static def downloadAndInstall = { url, binary , os ->
        def tmp = new File(binary.getParentFile(), binary.getName())
        FileUtils.copyURLToFile(new URL(url), tmp, 120000, 120000)
        binary.setExecutable(true)
        return binary
    }

    static def bin = Loggy.wrap({ x ->
        def location = x.params.location
        def version = x.version
        def os = x.os
        def arch = x.arch
        x.binary = LefthookInstallation.binary(location, version, os, arch)
        x.binary? x: null
    })

    static def binary = {location, version, os, arch ->
        def name = LefthookInstallation.getBinaryName(version, os, arch)
        return new File(location, name)
    }.memoize()


    static def getBinaryName = {version, os, arch ->
        def osId = os.id
        def archId = arch.id
        def extension = os == OS.Family.WINDOWS? ".exe": ""
        def name = "lefthook_${version}_${osId}_${archId}${extension}"
        return name
    }.memoize()

    static def artifact = Loggy.wrap({ x ->
        x = x + LefthookInstallation.resolveArtifact(x.params.repo, x.arch, x.os, x.params.version)
    })

    static def resolveArtifact = { String repo, OS.Arch arch, OS.Family os, String version ->
        def artifact = GitHubClient.resolve(repo, arch, os, version)
        return [url: artifact.url, version: artifact.version]
    }.memoize()

    static def os = Loggy.wrap({def x ->
        x.os = OS.getOs(x.params.os)
        x.os? x: null
    }.memoize())

    static def arch = Loggy.wrap({def x ->
        x.arch = OS.getArch(x.params.arch)
        x.arch? x: null
    }.memoize())

}
