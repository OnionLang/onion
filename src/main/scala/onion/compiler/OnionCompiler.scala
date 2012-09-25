/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005-2012, Kota Mizushima, All rights reserved.  *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package onion.compiler

import _root_.scala.collection.JavaConverters._
import _root_.scala.collection.Iterator
import java.io.BufferedReader
import java.io.IOException
import java.text.MessageFormat
import onion.compiler.toolbox._
import onion.compiler.exceptions.CompilationException

/**
 * @author Kota Mizushima
 * Date: 2005/04/08
 */
class OnionCompiler(val config: CompilerConfig) {

  def compile(fileNames: Array[String]): Array[CompiledClass] = {
    compile(fileNames.map {new FileInputSource(_)}:Array[InputSource])
  }

  def compile(srcs: Array[InputSource]): Array[CompiledClass] = {
    try {
      (new Parsing(config).andThen(new Typing(config)).andThen(new Generating(config)).process(srcs))
    } catch {
      case e: CompilationException =>
        for (error <- e.problems().asScala) printError(error)
        System.err.println(Messages("error.count", e.size))
        null
    }
  }

  private def printError(error: CompileError): Unit = {
    val location = error.location
    val sourceFile = error.sourceFile
    val message = new StringBuffer

    if (sourceFile == null) {
      message.append(MessageFormat.format("{0}", error.message))
    } else {
      var line: String = null
      var lineNum: String = null
      try {
        line = if (location != null) getLine(sourceFile, location.line) else ""
        lineNum = if (location != null) Integer.toString(location.line) else ""
      }
      catch {
        case e: IOException => {
          e.printStackTrace
        }
      }
      message.append(MessageFormat.format("{0}:{1}: {2}", sourceFile, lineNum, error.message))
      message.append(Systems.lineSeparator)
      message.append("\t\t")
      message.append(line)
      message.append(Systems.lineSeparator)
      message.append("\t\t")
      if (location != null)  message.append(getCursor(location.column))
    }
    System.err.println(new String(message))
  }

  private def getCursor(column: Int): String =  Strings.repeat(" ", column - 1) + "^"

  private def getLine(sourceFile: String, lineNumber: Int): String = {
    val reader = Inputs.newReader(sourceFile)
    try {
      val line = Iterator.continually(reader.readLine()).takeWhile(_ != null).zipWithIndex.map{ case (e, i) => (e, i + 1)}.find{
        case (e, i) => i == lineNumber
      }
      line.get._1
    } finally {
      reader.close
    }
  }
}