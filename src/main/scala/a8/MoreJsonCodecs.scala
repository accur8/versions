package a8

import a8.shared.json.{JsonCodec, JsonTypedCodec, ast}
import sttp.model.Uri

object MoreJsonCodecs {

  implicit val uri: JsonTypedCodec[Uri, ast.JsStr] =
    JsonCodec.string.dimap[Uri](
      s => Uri.parse(s).toOption.get,
      _.toString,
    )

}
