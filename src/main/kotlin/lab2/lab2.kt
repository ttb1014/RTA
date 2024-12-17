package lab2

import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

internal class Producer(
    private val queue: Queue<Int>,
    private val semaphoreProducer: Semaphore,
    private val semaphoreConsumer: Semaphore,
    private val counter: AtomicInteger
) : Runnable {
    override fun run() {
        try {
            while (true) {
                semaphoreProducer.acquire()
                synchronized(queue) {
                    val item = counter.incrementAndGet()
                    queue.add(item)
                    println("Produced: $item")
                }
                semaphoreConsumer.release()
                Thread.sleep(PRODUCER_DELAY)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

internal class Consumer(
    private val queue: Queue<Int>,
    private val semaphoreProducer: Semaphore,
    private val semaphoreConsumer: Semaphore,
) : Runnable {
    override fun run() {
        try {
            while (true) {
                semaphoreConsumer.acquire()
                synchronized(queue) {
                    val item = queue.poll()
                    println("Consumed: $item")
                }
                semaphoreProducer.release()
                Thread.sleep(CONSUMER_DELAY)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

internal class ThreadPool(poolSize: Int) {
    private val executorService: ExecutorService =
        Executors.newFixedThreadPool(poolSize) // Создаем пул потоков фиксированного размера

    fun submitTask(task: Runnable) {
        executorService.submit(task) // Добавляем задачу на выполнение
    }

    fun shutdown() {
        executorService.shutdownNow() // Останавливаем пул потоков
    }
}

const val QUEUE_SIZE = 10
const val THREAD_POOL_SIZE = 4
const val MAIN_THREAD_SLEEP_TIME = 10_000L
const val PRODUCER_DELAY = 100L
const val CONSUMER_DELAY = 2_000L

fun main(args: Array<String>) {
    val queue: Queue<Int> = LinkedList()
    val semaphoreProducer = Semaphore(QUEUE_SIZE)
    val semaphoreConsumer = Semaphore(0)
    val counter = AtomicInteger(0)
    val threadPool = ThreadPool(THREAD_POOL_SIZE)

    for (i in 0..THREAD_POOL_SIZE / 2) {
        threadPool.submitTask(Producer(queue, semaphoreProducer, semaphoreConsumer, counter))
        threadPool.submitTask(Consumer(queue, semaphoreProducer, semaphoreConsumer))
    }

    try {
        Thread.sleep(MAIN_THREAD_SLEEP_TIME)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    } finally {
        println("Выполнение остановлено")
        threadPool.shutdown()
    }
}