package com.omarkarimli.disco.utils

import com.omarkarimli.disco.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

object Updater {
    private val client = HttpClient()
    var lastCheckTime = -1L
        private set

    suspend fun getLatestVersionName(): Result<String> =
        runCatching {
            val response =
                client.get("https://api.github.com/repos/omarkarimli/Disco/releases/latest")
                    .bodyAsText()
            val json = JSONObject(response)
            val versionName = json.getString("name")
            lastCheckTime = System.currentTimeMillis()
            versionName
        }

    fun getLatestDownloadUrl(): String {
        val baseUrl = "https://github.com/omarkarimli/Disco/releases/latest/download/"
        val architecture = BuildConfig.ARCHITECTURE
        return if (architecture == "universal") {
            baseUrl + "Disco.apk"
        } else {
            baseUrl + "app-${architecture}-release.apk"
        }
    }
}
