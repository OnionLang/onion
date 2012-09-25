/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005-2012, Kota Mizushima, All rights reserved.  *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package onion.compiler.toolbox

/**
 * @author Kota Mizushima
 *         Date: 2005/06/22
 */
object Strings {
  def join(array: Array[String], separator: String): String = array.mkString(separator)

  def append(strings1: Array[String], strings2: Array[String]): Array[String] = strings1 ++ strings2

  def repeat(source: String, times: Int): String = Iterator.range(0, times).map {x => source}.mkString
}
