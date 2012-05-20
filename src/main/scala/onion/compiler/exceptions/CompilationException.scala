/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Kota Mizushima, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package onion.compiler.exceptions

import onion.compiler.CompileError
import java.util.Collections
import java.util.{Iterator => JIterator}
import java.util.{List => JList}
import java.lang.{Iterable => JIterable}

/**
 * @author Kota Mizushima
 * Exception thrown when compilation errors occured.
 */
class CompilationException(val problems_ : JList[CompileError]) extends RuntimeException with JIterable[CompileError] {

  def problems(): JList[CompileError] = Collections.unmodifiableList(problems)

  def size(): Int = problems.size();

  def iterator(): JIterator[CompileError] = problems.iterator()
}

