/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.*

public class HttpClientProvider {

    private static Call.Factory clientInstance

    static synchronized Call.Factory getClient() {
        if (clientInstance == null) {
            clientInstance = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build()
        }
        return clientInstance
    }

    static synchronized void setClient(Call.Factory client) {
        clientInstance = client
    }

    static void downloadToFile(String url, File file) throws IOException {
        File parent = file.getParentFile()
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs() && !parent.exists()) {
                throw new IOException("Failed to create directory: " + parent)
            }
        }
        Request request = new Request.Builder().url(url).build()
        try (Response response = getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file from ${url}: status ${response.code()}")
            }
            file.withOutputStream { output ->
                response.body().byteStream().withStream { input ->
                    output << input
                }
            }
        }
    }
}
