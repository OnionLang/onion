/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005-2012, Kota Mizushima, All rights reserved.  *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package onion.compiler

import java.io.IOException
import java.io.Reader

class StreamInputSource(var reader: Reader, name: String) extends InputSource {
  def openReader: Reader = reader
  def getName: String =  name
}