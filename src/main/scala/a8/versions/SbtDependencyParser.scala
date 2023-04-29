package a8.versions


import a8.versions.ast.{Dependency, Identifier, StringIdentifier, VariableIdentifier}
import io.accur8.neodeploy.model.Organization


object SbtDependencyParser {

  import fastparse.*, NoWhitespace.*

  def parse(dependenciesStr: String): Iterable[Dependency] = {
    fastparse.parse(dependenciesStr, dependencies(_)) match {
      case Parsed.Success(deps, _) =>
        deps
      case f: Parsed.Failure =>
        sys.error(f.msg)
    }
  }


  def dependencies[$: P] = P( dependency.rep(sep = ",") ~ ws0 ~ ",".? ~ ws0 ~ End )

  def ws[$: P] = P( CharsWhile(_.isWhitespace))
  def ws0[$: P] = P( ws.? )

  def dependency[$: P]: P[Dependency] =
    P(stringLit ~ scalaVersionSeparator ~ stringLit ~ separator ~ identifier ~ (separator ~ stringLit).? ~ exclusions.rep)
      .map { case (org, scalaArtifact, artifact, version, scope, exclusions) =>
        Dependency(
          organization = Organization(org),
          scalaArtifactSeparator = scalaArtifact,
          artifactName = artifact,
          version = version,
          configuration = scope,
          exclusions = exclusions,
        )
      }
//      .log()

  def exclusions[$: P]: P[(String, String)] =
    P(ws0 ~ "exclude(" ~ stringLit ~ op(()=>",") ~ stringLit ~ op(()=>")"))

  def scalaVersionSeparator[$: P] = P(op(()=>"%%%") | op(()=>"%%") | op(()=>"%")).!.map(_.trim)
  def separator[$: P] = op[$](()=>"%")

  def op[$: P](operator: ()=>String) = P(ws0 ~ operator())



  def identifier[$: P]: P[Identifier] =
    P(
      variable
      | stringLit.map(s => StringIdentifier(s))
    )

  def variable[$: P] = {
    val firstCharInIdentifier = P(CharPred(ch => ch.isLetter || ch == '_'))
    val remainingCharInIdentifier = P(firstCharInIdentifier | CharsWhile(_.isDigit))
    P(ws0 ~ (firstCharInIdentifier ~ remainingCharInIdentifier.rep).!)
      .map(VariableIdentifier(_))
  }

  def stringLit[$: P] = {
    val quoteChar = P("\"")
    val stringLitInside = P(CharsWhile(_ != '"').!)
    P(ws0 ~ quoteChar ~ stringLitInside.! ~ quoteChar)
//      .log()
  }

}
