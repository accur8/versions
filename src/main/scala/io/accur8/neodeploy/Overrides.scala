package io.accur8.neodeploy



object Overrides {

  lazy val isLinux = {
    val osName = System.getProperty("os.name", "generic")
    osName.toLowerCase().contains("nux")
  }

  lazy val sudoSystemCtlCommand: Command =
    if (isLinux) {
      Command("sudo", "systemctl")
    } else {
      Command("echo", "sudo", "systemctl")
    }

  lazy val sudoDockerCommand: Command =
    if (isLinux) {
      Command("sudo", "docker")
    } else {
      Command("echo", "sudo", "docker")
    }

  lazy val userSystemCtlCommand: Command =
    if ( isLinux ) {
      Command("systemctl")
    } else {
      Command("echo", "systemctl")
    }

  lazy val userLoginCtlCommand: Command =
    if (isLinux) {
      Command("loginctl")
    } else {
      Command("echo", "loginctl")
    }

  lazy val supervisorCtlCommand: Command =
    if ( isLinux ) {
      Command("supervisorctl")
    } else {
      Command("echo")
    }

}
