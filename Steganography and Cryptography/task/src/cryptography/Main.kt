package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (val input = readln()) {
            "exit" -> {
                println("Bye!")
                break
            }
            "hide" -> {
                println("Input image file:")
                val inputPicName = readln()
                println("Output image file:")
                val outputPicName = readln()
                println("Message to hide:")
                val secretMessage = readln().encodeToByteArray()

                println("Password:")
                val password = readln().encodeToByteArray()

                val encryption = mutableListOf<Byte>()
                
                var p = 0
                for (byte in secretMessage) {
                    encryption.add((byte.toInt() xor password[p].toInt()).toByte())
                    p++
                    if (password.lastIndex == p - 1) {
                        p = 0
                    }
                }
                val encryptedMessage = encryption.toByteArray()             
                    .plus(0b00000000)
                    .plus(0b00000000)
                    .plus(0b00000011)

                val pictureFile = File(inputPicName)
                try {
                    val picture: BufferedImage = ImageIO.read(pictureFile)

                    if (encryptedMessage.size * 8 > picture.width * picture.height) {
                        println("The input image is not large enough to hold this message.")
                        continue
                    }

                    var pixel = 0
                    var byteNumber = 0
                    var bitShift = 0
                    var stringBit: Int

                    row@ for (row in 0 until picture.height) {
                        for (col in 0 until picture.width ) {
                            try {
                                stringBit = encryptedMessage[byteNumber].toInt() shl bitShift and 0xff ushr 7
                                bitShift++
                            } catch (exc: IndexOutOfBoundsException) {
                                break@row   //if no more symbols left in secret string
                            }

                            val color = Color(picture.getRGB(col, row))
                            val newBlueValue = color.blue and 254 or stringBit

                            picture.setRGB(col, row, Color(color.red, color.green, newBlueValue).rgb)

                            pixel++
                            if (pixel % 8 == 0) {

                                byteNumber++    //switch to next byte of string
                                bitShift = 0    //nullify bit shift
                            }
                        }
                    }
                    ImageIO.write(picture, "png", File(outputPicName))

                } catch (exc: IOException) {
                    println("Can't read input file!")
                    continue
                }
                println("Message saved in $outputPicName image.")
            }
            "show" -> {
                println("Input image file:")

                try {
                    val picture = ImageIO.read(File(readln()))
                    println("Password:")
                    val password = readln().toByteArray()
                    var bits = 0
                    var pixel = 0
                    val encryptedMessage = mutableListOf<Byte>()
                    val secretMessage = mutableListOf<Byte>()

                    row@ for (row in 0 until picture.height) {
                        for (col in 0 until picture.width ) {

                            val color = Color(picture.getRGB(col, row))

                            bits = (bits shl 1) + (color.blue and 1)

                            pixel++
                            if (pixel % 8 == 0) {
                                encryptedMessage.add(bits.toByte())

                                val lastBytes = encryptedMessage.reversed().filterIndexed { index, _ -> index <= 2 }

                                if (lastBytes.size == 3 &&
                                    lastBytes.component1().toInt() == 3 &&
                                    lastBytes.component2().toInt() == 0 &&
                                    lastBytes.component3().toInt() == 0) {
                                    break@row
                                }
                                bits = 0
                            }
                        }
                    }

                    var p = 0
                    for (byte in encryptedMessage) {
                        secretMessage.add((byte.toInt() xor password[p].toInt()).toByte())
                        p++
                        if (password.lastIndex == p - 1) {
                            p = 0
                        }
                    }

                    println("Message:")
                    println(secretMessage.filterIndexed { index, _ ->
                        index != secretMessage.lastIndex || index != secretMessage.lastIndex - 1 ||
                                index != secretMessage.lastIndex - 2}.toByteArray().toString(Charsets.UTF_8))
                } catch (_: IOException ) {
                    println("Something wrong:")
                }
            }
            else -> println("Wrong task: $input")
        }
    }
}

