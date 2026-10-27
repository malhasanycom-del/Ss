package com.alrashid.iptv

object M3uParser {
    fun parse(text: String): List<Channel> {
        val result = mutableListOf<Channel>()
        var name: String? = null
        var group = "عام"
        var logo: String? = null

        text.lineSequence().forEach { raw ->
            val line = raw.trim().removePrefix("\uFEFF")
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val attrs = Regex("([A-Za-z0-9_-]+)=\"([^\"]*)\"")
                        .findAll(line)
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                    group = attrs["group-title"]?.takeIf { it.isNotBlank() } ?: "عام"
                    logo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    name = line.substringAfterLast(',', "").trim()
                        .ifBlank { attrs["tvg-name"].orEmpty() }
                }
                line.startsWith("http://", ignoreCase = true) ||
                    line.startsWith("https://", ignoreCase = true) -> {
                    result += Channel(
                        name = name?.takeIf { it.isNotBlank() } ?: "قناة ${result.size + 1}",
                        url = line,
                        group = group,
                        logo = logo
                    )
                    name = null
                    group = "عام"
                    logo = null
                }
            }
        }
        return result
    }
}
