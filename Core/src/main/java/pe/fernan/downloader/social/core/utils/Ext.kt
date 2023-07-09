package pe.fernan.downloader.social.core.utils

import java.security.MessageDigest
import java.util.UUID



fun String.md5() = this.uniqueId()

fun String.uniqueId(): String {

    val md5 = MessageDigest.getInstance("MD5")
    val digest = md5.digest(toByteArray())
    return digest.joinToString("") { byte -> "%02x".format(byte) }
}

fun String.uniqueIdError(): String {
    val inputString = "https"
    val md5Result = calculateMD5Hash(inputString)
    val uuid = generateUUIDFromMD5(md5Result)
    return uuid.toString()
}

fun String.toUUID(): UUID {
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        UUID.fromString(this.uniqueId())
    }
}


fun generateUUIDFromMD5(md5String: String): UUID {
    val md5Digest = MessageDigest.getInstance("MD5")
    val md5Bytes = md5Digest.digest(md5String.toByteArray())
    val mostSignificantBits = bytesToLong(md5Bytes.copyOfRange(0, 8))
    val leastSignificantBits = bytesToLong(md5Bytes.copyOfRange(8, 16))

    return UUID(mostSignificantBits, leastSignificantBits)
}

fun bytesToLong(byteArray: ByteArray): Long {
    var result: Long = 0
    for (i in byteArray.indices) {
        result = result shl 8 or (byteArray[i].toLong() and 0xFF)
    }
    return result
}

fun calculateMD5Hash(string: String): String {
    val md5Digest = MessageDigest.getInstance("MD5")
    val blockSize = 4096
    val bytes = string.toByteArray()
    var offset = 0

    while (offset < bytes.size) {
        val length = kotlin.math.min(blockSize, bytes.size - offset)
        md5Digest.update(bytes, offset, length)
        offset += length
    }

    val md5Bytes = md5Digest.digest()
    return md5Bytes.joinToString("") { "%02x".format(it) }
}

fun main() {
    listOf(
        "https://tikcdn.io/ssstik/aHR0cHM6Ly92MTZtLWRlZmF1bHQudGlrdG9rY2RuLXVzLmNvbS9iOGNlMzNiMWMwYmVjODMwNTg2MzE5MjE5YTE4MmQyNS82NGE0YzIyMi92aWRlby90b3MvdXNlYXN0NS90b3MtdXNlYXN0NS12ZS0wMDY4YzAwNC10eC9vSWZzaFM0NVFuQWVMS0VkZHM4a0RSa0FXRVhnZ0JpWlJwYmxiRC8/YT0wJmNoPTAmY3I9MCZkcj0wJmVyPTAmY2Q9MCU3QzAlN0MwJTdDMCZjdj0xJmJyPTMwMDImYnQ9MTUwMSZjcz0wJmRzPTMmZnQ9a0xNZHF5N29aenYwUEQxTWhkZlhnOXdtNllabWtFZUN+Jm1pbWVfdHlwZT12aWRlb19tcDQmcXM9MCZyYz1QR1JwT1R3M09HZzFOek0yWm1RMU5VQnBNM0k4Tnp3NlpqZG1hek16Wnpjek5FQmhMeTQyTm1GZk5UVXhNakpoTGpReFlTTTBYMmswY2pRd2FtZGdMUzFrTVM5emN3JTNEJTNEJmw9MjAyMzA3MDQxOTA2MzdBNDBBQzcwQjA3QzkwMzcwNjVFNyZidGFnPWUwMDAwMDAwMA==",
        "https://v16m-default.tiktokcdn-us.com/b8ce33b1c0bec830586319219a182d25/64a4c222/video/tos/useast5/tos-useast5-ve-0068c004-tx/oIfshS45QnAeLKEdds8kDRkAWEXggBiZRpblbD/?a=0&ch=0&cr=0&dr=0&er=0&cd=0%7C0%7C0%7C0&cv=1&br=3002&bt=1501&cs=0&ds=3&ft=kLMdqy7oZzv0PD1MhdfXg9wm6YZmkEeC~&mime_type=video_mp4&qs=0&rc=PGRpOTw3OGg1NzM2ZmQ1NUBpM3I8Nzw6ZjdmazMzZzczNEBhLy42NmFfNTUxMjJhLjQxYSM0X2k0cjQwamdgLS1kMS9zcw%3D%3D&l=20230704190637A40AC70B07C9037065E7&btag=e00000000",
        "https://tikcdn.io/ssstik/aHR0cHM6Ly9zZjE2LnRpa3Rva2Nkbi11cy5jb20vb2JqL2llcy1tdXNpYy10eC83MjM0Njk0MDUyMTAzMzU5Mjc1Lm1wMw=="
    ).forEach {
        println(it.md5() + " :::: " + it.md5().md5().md5())
    }

}
