/**
 * Auxiliary types and implicits that don't have to be imported (in
 * this package)
 *
 */

package object globair {

  def argError(msg: String): Nothing =
    throw new IllegalArgumentException(msg)

  def stateError(msg: String): Nothing =
    throw new IllegalStateException(msg)

  type Year = Int
  type Day = Int

  type Hours = Int
  type Minutes = Int


  import org.joda.time.Days
  implicit def toDays(day: Day) = new {
    def days: Days = Days.days(day)
  }

}
