/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import groovy.json.*
import okhttp3.*
import org.apache.commons.io.FileUtils

public class GitHubClient {

    static def resolve = { String repo, OS.Arch arch, OS.Family os, String version ->
        def params = [
            arch: arch,
            os: os,
            version: version,
            repo: repo
        ]
        def context = [params: params]
        def result = Optional.ofNullable(context)
            .map(x -> GitHubClient.release(x))
            .map(x -> GitHubClient.version(x))
            .map(x -> GitHubClient.url(x))
            .orElseThrow(() -> new RuntimeException("Unable to download lefthook"))
        return result

    }.memoize()

    static def url = { x ->
        x.url = GitHubClient.getUrl(x.release, x.params.os, x.params.arch)
        x.url? x: null
    }

    static def getUrl = { release, os, arch ->
        def asset = release.assets.find {
            Loggy.debug(it.name)
            it.name.contains(os.id) && it.name.contains(arch.id)
        }
        return asset?.browser_download_url
    }.memoize()

    static def version = { x ->
        x.version = x.release.tag_name
        Loggy.debug(x.version)
        x.version? x: null
    }

    static def release = { x ->
        x.release = GitHubClient.getRelease(x.params.repo, x.params.version)
        x.release? x: null
    }

    static def getRelease = { repo, version ->
 
        OkHttpClient okclient = new OkHttpClient()
            .newBuilder()
            .build()
        MediaType mediaType = MediaType.parse("application/vnd.github+json")
        //TODO allow full URL
        
        def url = "https://api.github.com/repos/${repo}/releases/latest"
        if(version != "latest") {
            url = "https://api.github.com/repos/${repo}/releases/tags/${version}"
        }
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .build()
        def result = null
        try(def response = okclient.newCall(request).execute()) {
            String content = response.body().string()
            Loggy.debug(content)
            def code = response.code
            def jsonSlurper = new JsonSlurper()
            result = jsonSlurper.parseText(content)
            if(code != 200)  {
                throw new IOException("Could not find release ${version} on repository ${repo}. status: ${code}")
            }
        }
        return result
  
    }.memoize()

}
