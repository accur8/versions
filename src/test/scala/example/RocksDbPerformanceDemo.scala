package example

import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future
import java.nio.charset.StandardCharsets
import zio.{Scope, ZIO, ZIOAppArgs, ZLayer, rocksdb}
import zio.rocksdb.RocksDB

object RocksDbPerformanceDemo extends zio.ZIOAppDefault {

  val writerCount = 100

  val byteCounters = new AtomicLong()

  val statsPeriod = 1000L

  type M[A] = zio.ZIO[RocksDB, Throwable, A]

  val buffer =
    (1 to 1024*1024)
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
          println(delta / (statsPeriod/1000) + "      " + newByteCount)
          previousByteCount = newByteCount
        }
      }
    }

  def startWriter(key: Int): M[Unit] =
    RocksDB.put(intToBytes(key), buffer)
      .flatMap { _ =>
        byteCounters.addAndGet(buffer.length)
        startWriter(key+1)
      }

  def intToBytes(v: Int): Array[Byte] = {
    val a = new Array[Byte](4)
    a(0) = (v & 0xff).toByte
    a(1) = ((v >> 8) & 0xff).toByte
    a(2) = ((v >> 16) & 0xff).toByte
    a(3) = ((v >> 24) & 0xff).toByte
    a
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    startStatsThread

    val database  = RocksDB.live("target/rocksdb")

    zio.ZIO.collectAllPar(
      (1 to writerCount)
        .map { i =>
          startWriter(i * 256 * 256 * 256)
        }
    ).provideLayer(database)

  }

}
