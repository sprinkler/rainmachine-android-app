package com.rainmachine.data.remote.util

object RemoteUtils {

    @JvmStatic
    fun toInt(bol: Boolean): Int = if (bol) 1 else 0

    @JvmStatic
    fun toBoolean(bolValue: Int): Boolean = bolValue != 0

    private val REGEX_MAC_ADDRESS = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"

    @JvmStatic
    fun isValidMacAddress(s: String): Boolean {
        return s.isNotBlank() && s.trim { it <= ' ' }.matches(REGEX_MAC_ADDRESS.toRegex())
    }

    @JvmStatic
    fun isValidURI(uri: String): Boolean {
        if (uri.isBlank()) {
            return false
        }

        return try {
            java.net.URI(uri)
            true
        } catch (throwable: Throwable) {
            // uri badly formed
            false
        }
    }

    @JvmStatic
    fun isValidInternetUrl(url: String): Boolean {
        return isValidURI(url) && (url.startsWith("http://") || url.startsWith("https://"))
    }

    @JvmStatic
    fun getDomainNameWithoutPort(url: String?): String {
        if (url.isNullOrBlank()) {
            return ""
        }
        val domainName = getDomainNameFromUrl(url)
        val endPos = domainName.lastIndexOf(':')
        return if (endPos == -1) domainName else domainName.substring(0, endPos)
    }

    @JvmStatic
    fun getDomainNameFromUrl(url: String?): String {
        if (url.isNullOrBlank()) {
            return ""
        }
        val cleanUrl = url!!
        var startPos = 0
        var endPos = cleanUrl.length
        if (cleanUrl.startsWith("https://")) {
            startPos = 8
        }
        if (cleanUrl.endsWith("/")) {
            endPos -= 1
        }
        val sb = StringBuilder()
        sb.append(cleanUrl, startPos, endPos)
        return sb.toString()
    }
}