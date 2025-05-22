package com.example.pronounceit.utils

import java.io.OutputStream
import java.io.RandomAccessFile

object WavUtil {
    fun writeWavHeader(out: OutputStream, totalAudioLen: Long, sampleRate: Int = 16000, channels: Int = 1, bitsPerSample: Int = 16) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val totalDataLen = totalAudioLen + 36

        val header = ByteArray(44)
        header[0] = 'R'.toByte(); header[1] = 'I'.toByte(); header[2] = 'F'.toByte(); header[3] = 'F'.toByte()
        writeInt(header, 4, totalDataLen.toInt())
        header[8] = 'W'.toByte(); header[9] = 'A'.toByte(); header[10] = 'V'.toByte(); header[11] = 'E'.toByte()
        header[12] = 'f'.toByte(); header[13] = 'm'.toByte(); header[14] = 't'.toByte(); header[15] = ' '.toByte()
        writeInt(header, 16, 16) // Subchunk1Size
        writeShort(header, 20, 1.toShort()) // AudioFormat PCM
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * bitsPerSample / 8).toShort())
        writeShort(header, 34, bitsPerSample.toShort())
        header[36] = 'd'.toByte(); header[37] = 'a'.toByte(); header[38] = 't'.toByte(); header[39] = 'a'.toByte()
        writeInt(header, 40, totalAudioLen.toInt())
        out.write(header, 0, 44)
    }

    fun writeWavHeader(raf: RandomAccessFile, totalAudioLen: Long, sampleRate: Int = 16000, channels: Int = 1, bitsPerSample: Int = 16) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val totalDataLen = totalAudioLen + 36

        val header = ByteArray(44)
        header[0] = 'R'.toByte(); header[1] = 'I'.toByte(); header[2] = 'F'.toByte(); header[3] = 'F'.toByte()
        writeInt(header, 4, totalDataLen.toInt())
        header[8] = 'W'.toByte(); header[9] = 'A'.toByte(); header[10] = 'V'.toByte(); header[11] = 'E'.toByte()
        header[12] = 'f'.toByte(); header[13] = 'm'.toByte(); header[14] = 't'.toByte(); header[15] = ' '.toByte()
        writeInt(header, 16, 16) // Subchunk1Size
        writeShort(header, 20, 1.toShort()) // AudioFormat PCM
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * bitsPerSample / 8).toShort())
        writeShort(header, 34, bitsPerSample.toShort())
        header[36] = 'd'.toByte(); header[37] = 'a'.toByte(); header[38] = 't'.toByte(); header[39] = 'a'.toByte()
        writeInt(header, 40, totalAudioLen.toInt())
        raf.seek(0)
        raf.write(header, 0, 44)
    }

    private fun writeInt(header: ByteArray, offset: Int, value: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = ((value shr 8) and 0xff).toByte()
        header[offset + 2] = ((value shr 16) and 0xff).toByte()
        header[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(header: ByteArray, offset: Int, value: Short) {
        header[offset] = (value.toInt() and 0xff).toByte()
        header[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }
}