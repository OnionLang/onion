/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2016-, Kota Mizushima, All rights reserved.  *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package onion.compiler.exceptions

/**
 * This class represents an exception while script is running.
 *
 * @author Kota Mizushima
 *
 */
class ScriptException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  /**
   * @param cause
   */
  def this(cause: Throwable) = {
    this("", cause)
  }
}
