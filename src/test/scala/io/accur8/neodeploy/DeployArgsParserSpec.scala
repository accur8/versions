package io.accur8.neodeploy


import a8.versions.model.BranchName
import cats.parse.Parser
import io.accur8.neodeploy.model.{DomainName, Version, VersionBranch}
import org.scalatest.funspec.AnyFunSpec

import scala.collection.mutable.Stack

class DeployArgsParserSpec extends AnyFunSpec {

  describe("DeployArgParser") {

    it("domainDeployable") {
      val result = DeployArgParser.parser.domainDeployable.parseAll("orbit.accur8.io:billybob").toOption.get
      assertResult(DomainName("orbit.accur8.io") -> "billybob")(result)
    }
    it("installDeployable") {

      type ResultType = Either[String,(DomainName, VersionBranch)]
      given CanEqual[ResultType, ResultType] = CanEqual.derived

      def test(value: String, expected: ResultType): Unit = {
        val actual: ResultType =
          DeployArgParser.parser.installDeployable.parseAll(value) match {
            case Left(e) =>
              Left(e.toString)
            case Right(v) =>
              Right(v)
          }
        assertResult(expected)(actual) : @annotation.nowarn
      }

      test(
        "qubes-dbsetup.accur8.io",
        Right(DomainName("qubes-dbsetup.accur8.io") -> VersionBranch.Empty),
      )

      test(
        "orbit.accur8.io",
        Right(DomainName("orbit.accur8.io") -> VersionBranch.Empty)
      )

      test(
        "orbit.accur8.io:install",
        Right(DomainName("orbit.accur8.io") -> VersionBranch.Empty)
      )

      test(
        "orbit.accur8.io:latest:install",
        Right(DomainName("orbit.accur8.io") -> VersionBranch.VersionBranchImpl(Version("latest"), None))
      )

      test(
        "orbit.accur8.io:latest:master:install",
        Right(DomainName("orbit.accur8.io") -> VersionBranch.VersionBranchImpl(Version("latest"), Some(BranchName("master"))))
      )

      test(
        "orbit.accur8.io:foo",
        Left("Error(15, NonEmptyList(EndOfString(15,19)))")
      )

    }

  }
}
