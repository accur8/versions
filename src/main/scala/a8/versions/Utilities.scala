package a8.versions


object Utilities {

  def isWindows: Boolean = {
    val os: String = System.getProperty("os.name").toLowerCase
    return (os.indexOf("win") >= 0)
  }

  lazy val resolvedGitExec: String =
    if (Utilities.isWindows) "git.exe"
    else "git"


  def using[T <: java.io.Closeable, R](resource: T)(block: T => R): R = {
    try { block(resource) }
    finally { resource.close() }
  }


  implicit class FileOps(file: java.io.File) {
    def write(bytes: Array[Byte]): Unit =
      using(new java.io.FileOutputStream(file))(_.write(bytes))
  }

}