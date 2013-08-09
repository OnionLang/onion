package onion.compiler


/**
 * Created with IntelliJ IDEA.
 * User: Mizushima
 * Date: 12/05/20
 * Time: 9:34
 * To change this template use File | Settings | File Templates.
 */
/**
 * @author Kota Mizushima
 *         Date: 2005/06/28
 */
class ClosureLocalBinding(val frameIndex: Int, index: Int, `type`: IRT.TypeRef) extends LocalBinding(index, `type`) {
  override def equals(other: Any): Boolean = {
    other match {
      case bind: ClosureLocalBinding =>
        if (frame != bind.frame) return false
        if (index != bind.index) return false
        if (vtype ne bind.vtype) return false
        true
      case _ =>
        false
    }
  }

  override def hashCode: Int = frame + index + vtype.hashCode

  private var frame: Int = 0
}
