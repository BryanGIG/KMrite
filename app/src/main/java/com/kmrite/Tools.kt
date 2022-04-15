package com.kmrite

import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.nio.ByteBuffer

data class Memory(val pkg: String) {
    var pid: Int = 0
    var sAddress: Long = 0L
}

object Tools {

    fun setCode(pkg: String, lib: String, offset: Int, hex: String): String {
        return try {
            val mem = Memory(pkg)
            getProcessID(mem)
            parseMap(mem, lib)
            memEdit(mem, offset, hex)
            "SUCCESS"
        } catch (e: Exception) {
            e.message!!
        }
    }

    private fun parseMap(nmax: Memory, lib_name: String) {
        val fil = File("/proc/${nmax.pid}/maps")
        if (fil.exists()) {
            fil.useLines { liness ->
                liness.forEach {
                    if (it.contains(lib_name) && nmax.sAddress == 0L) {
                        val lines = it.replace("\\s+".toRegex(), " ") //Removing WhiteSpace
                        val regex = "\\p{XDigit}+".toRegex()
                        val result: String = regex.find(lines)?.value!!
                        nmax.sAddress = result.toLong(16)
                    }
                }
            }
        } else {
            throw FileNotFoundException("FAILED OPEN DIRECTORY : ${fil.path}")
        }

        if (nmax.sAddress == 0L) {
            throw Exception("Unable to find the library")
        }
    }

    private fun memEdit(nmax: Memory, offset: Int, hex_t: String) {
        RandomAccessFile("/proc/${nmax.pid}/mem", "rw").use { channel ->
            channel.channel.use { channels ->
                val buff = ByteBuffer.wrap(hex2b(hex_t))
                for (i in 0 until hex_t.length / 2) {
                    channels.write(buff, (nmax.sAddress + offset) + i)
                }
            }
        }
    }

    private fun getProcessID(nmax: Memory) {

        try {
            val process = Runtime.getRuntime().exec(arrayOf("pidof", nmax.pkg))
            val reader = process.inputStream.bufferedReader().readLine()
            process.waitFor()
            nmax.pid = reader.toInt()
            process.destroy()
        } catch (e: Exception) {
            throw Exception("Unable to find the process")
        }

    }

    //Credit Code ("https://github.com/jbro129")
    private fun hex2b(hexs: String): ByteArray {
        var hex: String? = hexs
        if (hex!!.contains(" ")) {
            hex = hex.replace(" ", "")
        }
        return if (hex.length % 2 != 0) {
            throw IllegalArgumentException("Unexpected hex string: $hex")
        } else {
            val result = ByteArray(hex.length / 2)
            for (i in result.indices) {
                val d1 = decodeHexDigit(hex[i * 2]) shl 4
                val d2 = decodeHexDigit(hex[i * 2 + 1])
                result[i] = (d1 + d2).toByte()
            }
            result
        }
    }

    //Credit Code From ("https://github.com/jbro129")
    private fun decodeHexDigit(paramChar: Char): Int {
        if (paramChar in '0'..'9') {
            return paramChar - '0'
        }
        if (paramChar in 'a'..'f') {
            return paramChar - 'a' + 10
        }
        if (paramChar in 'A'..'F') {
            return paramChar - 'A' + 10
        }
        throw IllegalArgumentException("Unexpected hex digit: $paramChar")
    }
}