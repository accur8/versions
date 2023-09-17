package io.accur8.neodeploy.systemstate


import a8.shared.ZFileSystem.Directory
import a8.shared.{CompanionGen, FileSystem, SecretValue, StringValue}
import io.accur8.neodeploy.{DeployId, DnsService, HealthchecksDotIo, LocalDeploy}
import io.accur8.neodeploy.systemstate.MxSystemStateModel.*
import zio.{Task, Trace, ZIO, ZLayer}
import a8.shared.SharedImports.*
import a8.common.logging.{Logger, LoggingF}
import com.typesafe.config.Config
import io.accur8.neodeploy.model.{AppsInfo, AppsRootDirectory, CaddyDirectory, SupervisorDirectory}
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, ResolvedServer, ResolvedUser}

import java.nio.file.attribute.PosixFilePermission
import java.time.LocalDateTime

object SystemStateModel {

  object SecretContent extends SecretValue.Companion[SecretContent]
  case class SecretContent(value: String) extends SecretValue

  object StateKey extends MxStateKey {
    val empty = StateKey("empty", "")
  }
  @CompanionGen
  case class StateKey(kind: String, value: String) extends StringValue

  object UnixPerms extends StringValue.Companion[UnixPerms] {
    val empty = UnixPerms("")

    sealed abstract class Perm(val bit: Int) extends enumeratum.EnumEntry {
      val enumIndex = 2 - bit
    }
    object Perm extends enumeratum.Enum[Perm] {
      val values = findValues
      case object Execute extends Perm(0)
      case object Write extends Perm(1)
      case object Read extends Perm(2)
    }

    abstract sealed class UserClass(val name: String, val cardinality: Int) extends enumeratum.EnumEntry
    object UserClass extends enumeratum.Enum[UserClass] {
      val values = findValues
      case object Owner extends UserClass("owner", 0)
      case object Group extends UserClass("group", 1)
      case object Other extends UserClass("other", 2)
    }

    object PosixPermission {
      val values =
        for {
          perm <- Perm.values.toVector
          userClass <- UserClass.values
        } yield PosixPermission(perm, userClass)
    }

    case class PosixPermission(perm: Perm, userClass: UserClass) {
      lazy val nioPosixFilePermission = PosixFilePermission.values().apply(perm.enumIndex + userClass.cardinality*3)
      def fromOctalString(octalString: String): ResolvedPerm = {
        val digit = octalString.substring(userClass.cardinality, userClass.cardinality + 1).toInt
        val binaryStr = Integer.toBinaryString(digit).reverse + "   "
        val bitValue = binaryStr.charAt(perm.bit) == '1'
//        println(s"${this} -- ${digit} ${perm.bit} ${bitValue} ${octalString} ${binaryStr}")
        ResolvedPerm(this, bitValue)
      }
    }

    case class ResolvedPerm(posixPermission: PosixPermission, value: Boolean)

    def parse(octalValue: String): Vector[ResolvedPerm] = {
      if ( octalValue.isEmpty ) {
        Vector.empty
      } else if (octalValue.length == 4 && octalValue.substring(0,1) == "0") {
        parse(octalValue.substring((1)))
      } else if ( octalValue.length == 3 && octalValue.forall(_.isDigit) ) {
        PosixPermission
          .values
          .map(_.fromOctalString(octalValue))
      }  else {
        sys.error(s"invalid octal permission ${octalValue}")
      }
    }

  }
  case class UnixPerms(value: String) extends StringValue {
    lazy val expectedPerms = UnixPerms.parse(value)
    lazy val expectedPermsAsNioSet =
      expectedPerms
        .filter(_.value)
        .map(_.posixPermission.nioPosixFilePermission)
        .toSet
  }

  object PreviousState extends MxPreviousState
  @CompanionGen
  case class PreviousState(resolvedSyncState: ResolvedState) extends HasResolvedState

  object ResolvedState extends MxResolvedState
  @CompanionGen
  case class ResolvedState(deployId: DeployId, value: SystemState)

  object NewState extends MxNewState
  @CompanionGen
  case class NewState(resolvedSyncState: ResolvedState) extends HasResolvedState

  object Command extends MxCommand
  @CompanionGen
  case class Command(args: Iterable[String], workingDirectory: Option[Directory] = None) {
    def asRunnableCommand =
      io.accur8.neodeploy.Command(args, workingDirectory = workingDirectory)
  }

  sealed trait HasResolvedState {
    def isEmpty = SystemStateImpl.isEmpty(systemState)
    def systemState = resolvedSyncState.value
    def resolvedSyncState: ResolvedState
    lazy val statesByKey = SystemStateImpl.statesByKey(resolvedSyncState.value)
  }

  object SystemStateLogger {
    val simpleLayer =
      ZLayer.succeed(
        new SystemStateLogger {
          override def warn(message: String)(implicit trace: Trace): Task[Unit] =
            ZIO.attemptBlocking {
              Logger.logger(trace).warn(message)
            }
        }
      )
  }

  trait SystemStateLogger {
    def warn(message: String)(implicit trace: Trace): zio.Task[Unit]
  }

  object RunTimestamp {
    lazy val layer =
      ZLayer.fromZIO(
        zsucceed(RunTimestamp(LocalDateTime.now()))
      )
  }

  case class RunTimestamp(value: LocalDateTime) {
    lazy val asFileSystemCompatibleStr: String = {
      val now = value
      f"${now.getYear}${now.getMonthValue}%02d${now.getDayOfMonth}%02d_${now.getHour}%02d${now.getMinute}%02d${now.getSecond}%02d"
    }

  }

  type Environ = (
    SystemStateLogger
      with HealthchecksDotIo
      with ResolvedRepository
      with ResolvedServer
      with ResolvedUser
      with DnsService
      with LocalDeploy.Config
      with CaddyDirectory
  )


  type ApplyStateEnviron = AppsInfo with RunTimestamp

  type M[A] = zio.ZIO[Environ, Throwable, A]

  type ApplyState[A] = zio.ZIO[ApplyStateEnviron & Environ, Throwable, A]

}