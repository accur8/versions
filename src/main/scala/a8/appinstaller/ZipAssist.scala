package a8.appinstaller


import a8.shared.FileSystem.{Directory, File}
import a8.common.logging.Logging

import java.io.{FileInputStream, FileOutputStream, IOException, File => JFile}
import java.util.zip.{ZipEntry, ZipInputStream}

import language.implicitConversions
import predef._
import a8.versions.predef.using
import a8.shared.SharedImports._

object ZipAssist extends Logging {

//  def unzip(zipFile: File, dest: Directory, includePatterns: Option[String]): Unit = {
//    using(new ZipFile(zipFile.canonicalPath)) { antZipFile =>
//
//      val expand = new Expand() {
//
//        // standard boilerplate from here https://ant.apache.org/manual/antexternal.html
//        setProject(new Project())
//        getProject.init()
//        setTaskType("unzip")
//        setTaskName("unzip")
//        setOwningTarget(new Target())
//
//        // setup the ant task
//        setSrc(zipFile)
//        includePatterns.foreach { includes =>
//          val ps = new PatternSet
//          ps.setIncludes(includes)
//          addPatternset(ps)
//        }
//        setDest(dest)
//
//        //      override def log(msg: String): Unit =
//        //        logger.info("antlog -- " + msg)
//        //
//        //      override def log(msg: String,msgLevel: Int): Unit =
//        //        logger.info("antlog -- " + msgLevel + " -- " + msg)
//        //
//        //      override def log(msg: String, t: Throwable, msgLevel: Int): Unit =
//        //        logger.info("antlog -- " + msgLevel + " -- " + msg, t)
//
//      }
//      expand.execute()
//    }
//
//  }

//  def readEntryFromZipFile(zipFile: File, entryName: String): Option[String] = {
//    using(new ZipFile(zipFile)) { antZipFile =>
//      Option(antZipFile.getEntry(entryName))
//        .map(antZipFile.getInputStream)
//        .map(_.readString)
//    }
//  }

}
