import java.io.FilenameFilter
import sbt._
import Keys._
import classpath._
import Project.Initialize
import java.util.jar.{Attributes, Manifest}
import sbtassembly.Plugin._
import AssemblyKeys._

object Build extends Build {
  lazy val root = Project(
    id = "onion", 
    base = file("."), 
    settings = assemblySettings ++ onionSettigns
  )

  lazy val dist = TaskKey[Unit]("onion-dist")
  lazy val distPath = SettingKey[File]("onion-dist-path")

  private def manifestExtra(artifact: File, classpath: Classpath) = {
    val libs = classpath.map(_.data).filter(ClasspathUtilities.isArchive) :+ artifact
    val mf = new Manifest
    mf.getMainAttributes.put(Attributes.Name.CLASS_PATH, libs.map("lib/" + _.getName).mkString(" "))
    Package.JarManifest(mf)
  }

  private def distTask(target: File, out: File, artifact: File, classpath: Classpath) = {
    val libs = classpath.map(_.data).filter(ClasspathUtilities.isArchive)
    val libdir = out / "lib"
    IO.createDirectory(libdir)
    val map = libs.map(source => (source.asFile, libdir / source.getName))
    IO.copy(map)
    IO.copyDirectory(file("bin"), out / "bin")
    IO.copyDirectory(file("example"), out / "example")
    IO.copyFile(artifact, out / "onion.jar")
    IO.copyFile(file("README.md"), out / "README.md")
    val files = (out ***).get.flatMap(f=> f.relativeTo(out).map(r=>(f, r.getPath)))
    IO.zip(files, target / "onion-dist.zip")
  }

  private def javacc(classpath: Classpath, output: File, log: Logger): Seq[File] = {
    Fork.java(None, 
      "-cp" :: 
      Path.makeString(classpath.map(_.data)) ::
      List(
        "javacc",
        "-UNICODE_INPUT=true",
        "-JAVA_UNICODE_ESCAPE=true",
        "-OUTPUT_DIRECTORY=%s/onion/compiler/parser".format(output.toString),
        "grammar/JJOnionParser.jj"
      ),
      log
    ) match {
      case exitCode if exitCode != 0 => sys.error("Nonzero exit code returned from javacc: " + exitCode)
      case 0 =>
    }
    (output ** "*.java").get
  }

  lazy val onionSettigns = Defaults.defaultSettings ++ Seq(
    name := "onion",
    organization := "org.onion_lang",
    version := "1.0",
    scalaVersion := "2.11.0",
    unmanagedSourceDirectories in Compile <<= (Seq(javaSource in Compile) ++ Seq(scalaSource in Compile) ++ Seq(sourceManaged in Compile)).join,
    scalacOptions ++= Seq("-encoding", "utf8", "-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:existentials"),
    javacOptions ++= Seq("-sourcepath", "src.lib", "-source", "1.5"),
    libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-compiler" % _ ),
    libraryDependencies ++= Seq(
      "org.apache.bcel" % "bcel" % "5.2",
      "org.ow2.asm" % "asm" % "5.0.2",
      "net.java.dev.javacc" % "javacc" % "4.0",
      "junit" % "junit" % "4.7" % "test",
      "org.scalatest" %% "scalatest" % "2.1.6" % "test"
    ),
    sourceGenerators in Compile <+= (externalDependencyClasspath in Test, sourceManaged in Compile, streams) map { (cp, dir, s) =>
      val targetDir = dir / "java" / "onion" / "compiler" / "parser"
      if(!targetDir.isDirectory) {
        javacc(cp, dir / "java", s.log)
      } else {
        Seq()
      }
    },
    packageOptions in (Compile, packageBin) <<= (mainClass, packageOptions in (Compile, packageBin)) map { (main, opts) =>
      opts ++ main.map(Package.MainClass(_))
    },
    packageOptions in (Compile, packageBin) <<= (packageOptions in (Compile, packageBin), artifactPath in (Compile, packageBin), dependencyClasspath in Runtime) map { (opts, a, cp) =>
      opts :+ manifestExtra(a, cp)
    },
    dist <<= (target, distPath in dist, packageBin in Compile, fullClasspath in Runtime) map { (t, out, p, cp) =>
      distTask(t, out, p, cp)
    },
    distPath in dist <<= target(_ / "dist" ),
    mainClass := Some("onion.tools.OnionCompilerFrontend")
  )

}
