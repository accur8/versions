package example

import a8.versions.apps.Main

object RunMainDemo {

  def main(args: Array[String]) = {

//    Main.main(Array("resolve", "--organization", "a8", "--artifact", "a8-zoolander_2.12", "--branch", "master"))
//    Main.main(Array("gitignore"))
//    Main.main(Array("--help"))
//    Main.main(Array())

    Main.main(Array(
      "install",
      "--organization", "a8",
      "--artifact", "a8-qubes-dist_2.12",
      "--branch", "jeremy/ODIN-2026",
      "--install-dir", "/Users/flow/_a/test",
      "--lib-dir-kind", "Symlink",
      "--webapp-explode", "true",
    ))
//
//    Main.main(Array(
//      "install",
//      "--organization", "a8",
//      "--artifact", "a8-qubes-dist_2.12",
//      "--version", "2.7.1-20200623_0850_master",
//      "--install-dir", "/Users/flow/_a/test",
//      "--lib-dir-kind", "Symlink",
//      "--webapp-explode", "true",
//    ))
//
//    Main.main(Array(
//      "install",
//      "--organization", "a8",
//      "--artifact", "a8-qubes-dist_2.12",
//      "--branch", "master",
//      "--version", "2.7.1-20200623_0850_master",
//      "--install-dir", "/Users/flow/_a/test",
//      "--lib-dir-kind", "Symlink",
//      "--webapp-explode", "true",
//    ))
//
//    Main.main(Array(
//      "install",
//      "--organization", "a8",
//      "--artifact", "a8-qubes-dist_2.12",
////      "--branch", "master",
////      "--version", "2.7.1-20200623_0850_master",
//      "--install-dir", "/Users/flow/_a/test",
//      "--lib-dir-kind", "Symlink",
//      "--webapp-explode", "true",
//    ))

  }

}
