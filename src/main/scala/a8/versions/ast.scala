package a8.versions

import a8.shared.{Chord, CompanionGen}
import coursier.core.{ModuleName, Organization}
import a8.shared.SharedImports._
import a8.shared.json.{JsonCodec, JsonTypedCodec}
import a8.versions.Mxast._

object ast {

  import model.impl.q

  object Repo extends MxRepo

  @CompanionGen
  case class Repo(
    header: Option[String] = None,
    organization: String,
    gradle: Boolean = false,
    public: Boolean = false,
    modules: Iterable[Module],
  )


  object Module extends MxModule

  @CompanionGen
  case class Module(
    sbtName: String,
    projectType: Option[String],
    artifactName: Option[String],
    directory: Option[String],
    dependsOn: Iterable[String] = Nil,
    dependencies: Option[String],
    jvmDependencies: Option[String],
    jsDependencies: Option[String],
    extraSettings: Option[String],
  )

  object Dependency extends MxDependency

  @CompanionGen
  case class Dependency(
    organization: String,
    scalaArtifactSeparator: String,
    artifactName: String,
    version: Identifier,
    configuration: Option[String] = None,
    exclusions: Iterable[(String, String)] = Nil,
  ) {

    lazy val asCoursierModule =
      coursier.Module(Organization(organization), ModuleName(artifactName + (if (scalaArtifactSeparator == "%%") "_2.12" else "")))


    def exclusionsAsSbt =
      exclusions
        .map { exclusion =>
          Chord.str("exclude(") ~ q(exclusion._1) ~ ", " ~ q(exclusion._2) ~ ")"
        }
        .mkChord(Chord.str(" "))

    val configurationAsSbt: Option[Chord] =
      configuration.map(c => Chord.str("% ") ~ q(c))

    def asSbt(versionDotPropsMap: Map[String,String]) = {
      q(organization) ~ " " ~ scalaArtifactSeparator ~ " " ~ q(artifactName) ~ " % " ~ version.asScala ~ " " ~ configurationAsSbt.map(_ ~ " ").getOrElse(Chord.empty) ~ exclusionsAsSbt
    }
  }

  object Identifier {
    implicit lazy val codec: JsonCodec[Identifier] =
      JsonTypedCodec
        .string
        .dimap[Identifier](
          s =>
            if ( s.charAt(0).isLetter )
              ast.VariableIdentifier(s)
            else
              ast.StringIdentifier(s)
          ,
          _.rawValue
        )
        .asJsonCodec
  }

  sealed trait Identifier {
    def rawValue: String
    def asScala: Chord
  }

  case class VariableIdentifier(name: String) extends Identifier {
    override def rawValue: String = name
    override def asScala = Chord.str(name)
  }

  case class StringIdentifier(value: String) extends Identifier {
    override def rawValue: String = value
    override def asScala = q(value)
  }


}
