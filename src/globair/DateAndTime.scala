package globair

import DatabaseDSL.IntField
import java.util.Calendar

sealed abstract class WeekDay(val ord: Int) extends IntField {
    def rep = ord
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
  def apply(x: Int): WeekDay = intToWeekDay(x)
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

object Date {

  def isLeap(year: Year): Boolean =
    if (year % 400 == 0) {
      true
    } else if (year % 100 == 0) {
      false
    } else if (year % 4 == 0) {
      true
    } else {
      false
    }

  val daysPerMonth: Function[(Month, Year), Int] = {
    case (February, year) => if (isLeap(year)) 29 else 28
    case (April | June | September | November, _) => 30
    case _ => 31
  }

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

case class Date(day: Day, month: Month, year: Year) extends Ordered[Date] {
  require(year > 2000, "A year must be greater than 2000")
  require(day > 0, "The day must be a positive number")
  require(day <= Date.daysPerMonth(month, year), "Invalid day")

  def compare(that: Date): Int = {
    val Date(dayA, monthA, yearA) = this
    val Date(dayB, monthB, yearB) = that

    // Easy way
    val strA = "%04d%02d%02d" format(yearA, monthA, dayA)
    val strB = "%04d%02d%02d" format(yearB, monthB, dayB)

    strA compare strB
  }

  // TODO switch  to scala-time
  def +(days: Int): Date = null

  /**
   * d.in(startDate, endDate) == True iff startDate <= d && d <= endDate
   */
  def in(startDate: Date, endDate: Date): Boolean =
    startDate <= this && this <= endDate

  override lazy val toString = "%04d/%02d/%02d" format(year, month, day)

  private lazy val calendar = {
    val cal = Calendar.getInstance()
    cal.set(year, month.ord - 1, day)
    cal
  }

  lazy val dayOfWeek: WeekDay = WeekDay(calendar.get(Calendar.DAY_OF_WEEK) + 1)
  lazy val dayOfYear: Day = calendar.get(Calendar.DAY_OF_YEAR)

  /**
   * Return a Seq of Dates of all days that are a `weekDay` between
   * `this` and `to` (both inclusive).
   */
  def weekDaysUntil(weekDay: WeekDay, to: Date): Seq[Date] = {
    // the first multiple of 7 + weekDay.ord - 1 that > this.dayOfYear
    var start = this
    while (start.dayOfWeek != weekDay) {
      start += 1
    }
    var day = start
    var days: List[Date] = Nil
    while (day <= to) {
      days ::= day
      day += 7
    }
    days
  }

  import java.sql.Timestamp
  def toTimestamp: Timestamp = new Timestamp(year, month.ord, day, 0, 0, 0, 0)

}

case class Time(hours: Hours, minutes: Minutes) extends Ordered[Time] {
  require(0 <= hours && hours < 24, "Hours must be between 0 (inclusive) and 24 (exclusive)")
  require(0 <= minutes && minutes < 60, "Minutes must be between 0 (inclusive) and 60 (exclusive)")

  def compare(that: Time): Int =
    (this.hours + this.minutes * 60) compare (that.hours + that.minutes * 60)

  override lazy val toString = "%02d:%02d" format(hours, minutes)

}
