package lab3

import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

internal class Producer(private val outputStream: PipedOutputStream) : Runnable {
    override fun run() {
        try {
            for (i in 0..9) {
                val message = "Message $i "
                outputStream.write(message.toByteArray())
                println("Written: $message")
                Thread.sleep(PRODUCER_DELAY)
            }
        } catch (e: IOException) {
            Thread.currentThread().interrupt()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } finally {
            try {
                outputStream.close()
                println("Producer thread shutdown")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

internal class Consumer(private val inputStream: PipedInputStream) : Runnable {
    override fun run() {
        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int

        try {
            while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                val message = String(buffer, 0, bytesRead)
                println("Read: $message ")
                Thread.sleep(CONSUMER_DELAY)
            }
        } catch (e: IOException) {
            Thread.currentThread().interrupt()
        } finally {
            try {
                inputStream.close()
                println("Consumer thread shutdown")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

const val BUFFER_SIZE = 1024
const val PRODUCER_DELAY = 400L
const val CONSUMER_DELAY = 0L

fun main(args: Array<String>) {
    val outputStream = PipedOutputStream()
    var inputStream: PipedInputStream? = null

    try {
        inputStream = PipedInputStream(outputStream)

        val producer = Thread(Producer(outputStream))
        val consumer = Thread(Consumer(inputStream))

        producer.start()
        consumer.start()

        producer.join()
        consumer.join()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } finally {
        println("End of program")
    }
}
