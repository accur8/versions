package example

import zio.rocksdb.RocksDB
import zio.{Scope, ZIO, ZIOAppArgs, ZLayer, rocksdb}

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future
import zio.*
import zio.nio.channels.*
import zio.nio.file.*

import java.io.IOException

object ZioNioPerformanceDemo extends zio.ZIOAppDefault {

  val writerCount = 1

  val byteCounters = new AtomicLong()

  val statsPeriod = 1000L

  type M[A] = zio.ZIO[Scope, Throwable, A]

  val bufferA =
    (1 to 1024*16)
      .map(_.toByte)
      .toArray

  val buffer = Chunk.fromArray(bufferA)

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

  def startWriter(index: Int): M[Unit] = {
    Path(s"target/streamer-${index}")
      .toAbsolutePath
      .flatMap{ path =>
        AsynchronousFileChannel.open(
          path,
          java.nio.file.StandardOpenOption.WRITE,
          java.nio.file.StandardOpenOption.CREATE,
        ).flatMap { channel =>
          def write(pos: Long): M[Unit] = {
//            println(s"write ${pos}")
            channel.writeChunk(buffer, pos)
              .flatMap { _ =>
                byteCounters.addAndGet(buffer.size)
                write(pos + buffer.size)
              }
          }
          write(0)
        }
      }
  }
//    RocksDB.put(intToBytes(key), buffer)
//      .flatMap { _ =>
//        byteCounters.addAndGet(buffer.length)
//        startWriter(key+1)
//      }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    startStatsThread

    zio.ZIO.collectAllPar(
      (1 to writerCount)
        .map { i =>
          startWriter(i)
        }
    )

  }

}
