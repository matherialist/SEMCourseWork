package com.holeaf.api.utils

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.random.Random

object AeSimpleSHA1 {
    private fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            val i = b.toInt()
            var halfbyte: Int = i ushr 4 and 0x0F
            var two_halfs = 0
            do {
                buf.append(if (0 <= halfbyte && halfbyte <= 9) ('0'.toInt() + halfbyte).toChar() else ('a'.toInt() + (halfbyte - 10)).toChar())
                halfbyte = i and 0x0F
            } while (two_halfs++ < 1)
        }
        return buf.toString()
    }

    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    fun SHA1(text: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val textBytes = text.toByteArray(charset("iso-8859-1"))
        md.update(textBytes, 0, textBytes.size)
        val sha1hash = md.digest()
        return convertToHex(sha1hash)
    }
}

val salt = "HolySalt!"

fun passwordHash(password: String): String {
    return AeSimpleSHA1.SHA1(salt + password).substring(0, 32)
}

fun generateString(length: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { i -> Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}