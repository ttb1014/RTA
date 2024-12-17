package lab1

import java.util.*
import java.util.concurrent.Semaphore

internal class ProducerConsumer {
    private val buffer: Queue<Int> = LinkedList()
    private val semaphoreConsume = Semaphore(0)
    private val semaphoreProduce = Semaphore(BUFFER_SIZE)
    private val mutex = Semaphore(1)

    @Throws(InterruptedException::class)
    fun produce(item: Int) {
        semaphoreProduce.acquire()
        mutex.acquire()
        buffer.add(item)
        println("Produced: $item")
        mutex.release()
        semaphoreConsume.release()
    }

    @Throws(InterruptedException::class)
    fun consume(): Int {
        semaphoreConsume.acquire()
        mutex.acquire()
        val item = buffer.poll()
        println("Consumed: $item")
        mutex.release()
        semaphoreProduce.release()
        return item
    }

    companion object {
        private const val BUFFER_SIZE = 5

        @JvmStatic
        fun main(args: Array<String>) {
            val pc = ProducerConsumer()
            val producer = Producer(pc)
            val consumer = Consumer(pc)
            producer.start()
            consumer.start()
        }
    }
}

internal class Producer(private val pc: ProducerConsumer) : Thread() {
    override fun run() {
        try {
            for (i in 0..9) {
                pc.produce(i)
                sleep(1000L)
            }
        } catch (e: InterruptedException) {
            currentThread().interrupt()
        }
    }
}

internal class Consumer(private val pc: ProducerConsumer) : Thread() {
    override fun run() {
        try {
            for (i in 0..9) {
                pc.consume()
                sleep(2000L)
            }
        } catch (e: InterruptedException) {
            currentThread().interrupt()
        }
    }
}