package example

import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicLong
import scala.annotation.nowarn
import scala.concurrent.Future

object StreamPerformanceDemo {

  val writerCount = 1

  val byteCounters = new AtomicLong()

  val statsPeriod = 1000L

  val buffer =
    (1 to 1024*16)
      .map(_.toByte)
      .toArray


  def startStatsThread: Thread =
    new Thread() {
      start()
      override def run(): Unit = {
        var previousByteCount = 0L
        while( true ) {
          Thread.sleep(statsPeriod)
          val newByteCount = byteCounters.get()
          val delta = newByteCount - previousByteCount
          println((delta / (statsPeriod/1000)).toString + "      " + newByteCount)
          previousByteCount = newByteCount
        }
      }
    }

  def startWriters: Iterable[Thread] =
    (1 to writerCount)
      .map { i =>
        new Thread {
          start()
          override def run(): Unit = {
            val file = new java.io.File(s"/Users/home/big-files/target/stream-${i}")
            val out = new FileOutputStream(new java.io.File(s"/Users/home/big-files/target/stream-${i}"))
            while ( true ) {
              out.write(buffer)
              byteCounters.addAndGet(buffer.length): @nowarn
            }
          }
        }
      }

  def main(args: Array[String]): Unit = {
    startStatsThread: @nowarn
    startWriters
      .foreach(_.join())
  }

}
