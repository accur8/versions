package io.accur8.neodeploy


import a8.common.logging.Logging
import io.accur8.neodeploy.model.AuthorizedKey

import java.net.URL
import java.nio.charset.StandardCharsets

object CodeBits extends Logging {

  def downloadAsString(url: String): Option[String] = {
    val u = new URL(url)
    val in = u.openStream
    try {
      Some(new String(in.readAllBytes, StandardCharsets.UTF_8))
    } catch {
      case e: Exception =>
        logger.warn(s"downloading of ${url} failed", e)
        None
    } finally {
      if (in != null) in.close()
    }
  }

  def downloadKeys(url: String): Vector[AuthorizedKey] =
    for {
      allKeysStr <- downloadAsString(url).toVector
      key <-
        allKeysStr
          .linesIterator
          .map(AuthorizedKey.apply)
          .toVector
    } yield key

}
