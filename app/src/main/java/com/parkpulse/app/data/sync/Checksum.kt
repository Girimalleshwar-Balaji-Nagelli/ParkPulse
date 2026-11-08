package com.parkpulse.app.data.sync

import java.security.MessageDigest

object Checksum {
    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
