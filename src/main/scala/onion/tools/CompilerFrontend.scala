/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2016-, Kota Mizushima, All rights reserved.  *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package onion.tools

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import onion.compiler.CompiledClass
import onion.compiler.OnionCompiler
import onion.compiler.CompilerConfig
import onion.compiler.exceptions.ScriptException
import onion.compiler.toolbox.Message
import onion.compiler.toolbox.Systems
import onion.tools.option._

/**
 *
 * @author Kota Mizushima
 *
 */
object CompilerFrontend {
  private def config(option: String, requireArg: Boolean): OptionConfig = new OptionConfig(option, requireArg)

  private def pathArray(path: String): Array[String] = path.split(Systems.pathSeparator)

  private def printError(message: String): Unit = System.err.println(message)

  def main(args: Array[String]): Unit = {
    try {
      new CompilerFrontend().run(args)
    } catch {
      case e: ScriptException => throw e.getCause
    }
  }

  private final val CLASSPATH: String = "-classpath"
  private final val SCRIPT_SUPER_CLASS: String = "-super"
  private final val ENCODING: String = "-encoding"
  private final val OUTPUT: String = "-d"
  private final val MAX_ERROR: String = "-maxErrorReport"
  private final val DEFAULT_CLASSPATH: Array[String] = Array[String](".")
  private final val DEFAULT_ENCODING: String = System.getProperty("file.encoding")
  private final val DEFAULT_OUTPUT: String = "."
  private final val DEFAULT_MAX_ERROR: Int = 10
}

class CompilerFrontend {

  import CompilerFrontend._

  private val commandLineParser = new CommandLineParser(config(CLASSPATH, true), config(SCRIPT_SUPER_CLASS, true), config(ENCODING, true), config(OUTPUT, true), config(MAX_ERROR, true))

  def run(commandLine: Array[String]): Int = {
    if (commandLine.length == 0) {
      printUsage
      return -1
    }
    val result: Option[ParseSuccess] = parseCommandLine(commandLine)
    result match {
      case None => -1
      case Some(success) =>
        val config: Option[CompilerConfig] = createConfig(success)
        val params: Array[String] = success.arguments.toArray
        if (params.length == 0) {
          printUsage
          return -1
        }
        config match {
          case None => -1
          case Some(config) =>
            val classes = compile(config, params)
            (for (cs <- classes) yield {
              val succeed = generateFiles(cs)
              if (succeed) 0 else -1
            }).getOrElse(-1)
        }
    }
  }

  private def simpleNameOf(fqcn: String): String = {
    val index = fqcn.lastIndexOf(".")
    if (fqcn.lastIndexOf(".") < 0)  fqcn else fqcn.substring(index + 1, fqcn.length)
  }

  private def outputPathOf(outDir: String, fqcn: String): String = outDir + Systems.fileSeparator + simpleNameOf(fqcn)+ ".class"

  private def generateFiles(binaries: Seq[CompiledClass]): Boolean = {
    val generated: java.util.List[File] = new java.util.ArrayList[File]
    for(binary <- binaries) {
      val outDir: String = binary.outputPath
      new File(outDir).mkdirs
      val outPath: String = outputPathOf(outDir, binary.className)
      val targetFile: File = new File(outPath)
      try {
        if (!targetFile.exists) targetFile.createNewFile
        generated.add(targetFile)
        using(new BufferedOutputStream(new FileOutputStream(targetFile))){out =>
          out.write(binary.content)
        }
      } catch {
        case e: IOException =>
          val it = generated.iterator
          while (it.hasNext) {
            it.next.delete
          }
          return false
      }
    }
    true
  }

  protected def printUsage {
    printError(
      """Usage: onionc [-options] source_file ...
        |options:
        | -super <super class>        specify script's super class
        | -d <path>                   specify output directory
        | -classpath <path>           specify classpath
        | -encoding <encoding>        specify source file encoding
        | -maxErrorReport <number>    set number of errors reported""".stripMargin)
  }

  private def parseCommandLine(commandLine: Array[String]): Option[ParseSuccess] = {
    val result = commandLineParser.parse(commandLine)
    result match {
      case success: ParseSuccess => Some(success)
      case failure: ParseFailure =>
        val lackedOptions = failure.lackedOptions
        val invalidOptions = failure.invalidOptions
        invalidOptions.foreach{opt => printError(Message.apply("error.command.invalidArgument", opt)) }
        lackedOptions.foreach{opt => printError(Message.apply("error.command..noArgument", opt)) }
        None
    }
  }

  private def createConfig(result: ParseSuccess): Option[CompilerConfig] = {
    val option: Map[String, CommandLineParam] = result.options.toMap
    val classpath: Array[String] = checkClasspath(
      option.get(CLASSPATH).collect{ case ValuedParam(value) => value }
    )
    val encoding: Option[String] = checkEncoding(
      option.get(ENCODING).collect{ case ValuedParam(value) => value }
    )
    val outputDirectory: String = checkOutputDirectory(
      option.get(OUTPUT).collect{ case ValuedParam(value) => value}
    )
    val maxErrorReport: Option[Int] = checkMaxErrorReport(
      option.get(MAX_ERROR).collect{ case ValuedParam(value) => value}
    )
    for (e <- encoding; m <- maxErrorReport) yield {
      new CompilerConfig(classpath, "", e, outputDirectory, m)
    }
  }

  private def compile(config: CompilerConfig, fileNames: Array[String]): Option[Seq[CompiledClass]] = {
    Option(new OnionCompiler(config).compile(fileNames))
  }

  private def checkClasspath(classpath: Option[String]): Array[String] = {
    (for (c <- classpath) yield pathArray(c)).getOrElse(DEFAULT_CLASSPATH)
  }

  private def checkOutputDirectory(outputDirectory: Option[String]): String = outputDirectory.getOrElse(DEFAULT_OUTPUT)

  private def checkEncoding(encoding: Option[String]): Option[String] = {
    try {
      (for (e <- encoding) yield {
        "".getBytes(e)
        e
      }).orElse(Some(DEFAULT_ENCODING))
    } catch {
      case e: UnsupportedEncodingException => {
        printError(Message.apply("error.command.invalidEncoding", ENCODING))
        None
      }
    }
  }

  private def checkMaxErrorReport(maxErrorReport: Option[String]): Option[Int] = {
    try {
      maxErrorReport match {
        case Some(m) =>
          val value = Integer.parseInt(m)
          if (value > 0) Some(value) else None
        case None => Some(DEFAULT_MAX_ERROR)
      }
    } catch {
      case e: NumberFormatException =>
        printError(Message.apply("error.command.requireNaturalNumber", MAX_ERROR))
        None
    }
  }

}
