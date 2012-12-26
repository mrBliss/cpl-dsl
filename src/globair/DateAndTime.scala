package globair

/**
 * Auxiliary classes for times: WeekDay, Month, Date, Time
 * Date is based on joda-time's LocalDate.
 *
 */

import DBDSL.IntField
import org.joda.time.{LocalDate, ReadablePeriod, Interval}

sealed abstract class WeekDay(val ord: Int) extends IntField {
    def rep = ord

  /**
   * Return a Seq of Dates of all days that are this week day in the given
   * range (start and end date included).
   */
  def in(interval: Interval): Seq[Date] = {
    // the first multiple of 7 + weekDay.ord - 1 that > this.dayOfYear
    var start = new Date(interval.getStart.toLocalDate)
    val end = new Date(interval.getEnd.toLocalDate)
    // Look for the first day after start that is this week day
    while (start.dayOfWeek != this) start += 1.days
    var day = start
    val days = new collection.mutable.ListBuffer[Date]
    while (day <= end) {
      days += day
      day += 7.days
    }
    days.toList
  }

}
case object Sunday extends WeekDay(1)
case object Monday extends WeekDay(2)
case object Tuesday extends WeekDay(3)
case object Wednesday extends WeekDay(4)
case object Thursday extends WeekDay(5)
case object Friday extends WeekDay(6)
case object Saturday extends WeekDay(7)

object WeekDay {
  private lazy val intToWeekDay = Map[Int, WeekDay] {
    1 -> Sunday
    2 -> Monday;
    3 -> Tuesday
    4 -> Wednesday;
    5 -> Thursday;
    6 -> Friday;
    7 -> Saturday;
  }
  def apply(x: Int): Option[WeekDay] = intToWeekDay get x
}

sealed abstract class Month(val ord: Int)
case object January extends Month(1)
case object February extends Month(2)
case object March extends Month(3)
case object April extends Month(4)
case object May extends Month(5)
case object June extends Month(6)
case object July extends Month(7)
case object August extends Month(8)
case object September extends Month(9)
case object October extends Month(10)
case object November extends Month(11)
case object December extends Month(12)

object Month {
  private lazy val intToMonth = Map[Int, Month] {
    1 -> January;
    2 -> February;
    3 -> March;
    4 -> April;
    5 -> May;
    6 -> June;
    7 -> July;
    8 -> August;
    9 -> September;
    10 -> October;
    11 -> November;
    12 -> December;
  }
  def apply(x: Int): Option[Month] = intToMonth get x
}

object Date {

  def apply(year: Year, month: Month, day: Day) =
    new Date(new LocalDate(year, month.ord, day))

  def unapply(date: Date): Option[(Day, Month, Year)] =
    Some(date.day, date.month, date.year)

  implicit def dateSyntax(day: Int) = new {
    def January(year: Year): Date = Date(day, globair.January, year)
    def February(year: Year): Date = Date(day, globair.February, year)
    def March(year: Year): Date = Date(day, globair.March, year)
    def April(year: Year): Date = Date(day, globair.April, year)
    def May(year: Year): Date = Date(day, globair.May, year)
    def June(year: Year): Date = Date(day, globair.June, year)
    def July(year: Year): Date = Date(day, globair.July, year)
    def August(year: Year): Date = Date(day, globair.August, year)
    def September(year: Year): Date = Date(day, globair.September, year)
    def October(year: Year): Date = Date(day, globair.October, year)
    def November(year: Year): Date = Date(day, globair.November, year)
    def December(year: Year): Date = Date(day, globair.December, year)
  }

}

class Date(val date: LocalDate) extends Ordered[Date] {

  def day: Day = date.dayOfMonth.get
  def month: Month = Month(date.monthOfYear.get).get // the last get cannot fail
  def year: Year = date.year.get

  def compare(that: Date): Int = this.date compareTo that.date

  def +(period: ReadablePeriod): Date = new Date(date plus period)
  def -(period: ReadablePeriod): Date = new Date(date minus period)

  def ->(endDate: Date): Interval =
    new Interval(this.date.toDateTimeAtStartOfDay,
                 endDate.date.toDateTimeAtStartOfDay)

  def in(interval: Interval) = interval.contains(date.toDateTimeAtStartOfDay)

  def in(startDate: Date, endDate: Date): Boolean = in(startDate -> endDate)

  override lazy val toString = date.toString

  lazy val dayOfWeek: WeekDay = WeekDay(date.dayOfWeek.get).get
  lazy val dayOfYear: Day = date.dayOfYear.get


  import java.sql.Timestamp
  def toTimestamp: Timestamp = new Timestamp(date.toDateTimeAtStartOfDay.getMillis)

}


case class Time(hours: Hours, minutes: Minutes) extends Ordered[Time] {
  require(0 <= hours && hours < 24, "Hours must be between 0 (inclusive) and 24 (exclusive)")
  require(0 <= minutes && minutes < 60, "Minutes must be between 0 (inclusive) and 60 (exclusive)")

  def compare(that: Time): Int =
    (this.hours + this.minutes * 60) compare (that.hours + that.minutes * 60)

  override lazy val toString = "%02d:%02d" format(hours, minutes)

}
