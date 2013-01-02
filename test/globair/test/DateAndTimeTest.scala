package globair.test

import org.scalatest.FunSuite
import scala.collection.mutable.Stack


class DateAndTimeSuite extends FunSuite {
  import globair.WeekDay
  import globair.WeekDay._

  test("Weekday's apply function") {
    assert(WeekDay(1).get === Monday)
    assert(WeekDay(7).get === Sunday)
  }

  import globair.Month
  import globair.Month._

  test("Month's apply function") {
    assert(Month(1).get === January)
    assert(Month(12).get === December)
  }

  import globair.Date
  import globair.Date._

  test("Date's apply function") {
    val d = Date(19, February, 2012)
    assert(d === (19 February 2012))
    assert(d.day === 19)
    assert(d.month === February)
    assert(d.year === 2012)
    intercept[org.joda.time.IllegalFieldValueException] {
      Date(30, February, 2012)
    }
  }

  import globair.toDays
  test("Date arithmetic") {
    assert((19 February 2012) + 2.days === (21 February 2012))
    assert((19 February 2012) - 2.days === (17 February 2012))
    assert((28 February 2012) + 2.days === (1 March 2012))
    assert((31 December 2012) + 3.days === (3 January 2013))
  }

  test("Day of week") {
    assert((27 December 2012).dayOfWeek === Thursday)
    assert((20 December 2012).dayOfWeek === Thursday)
    assert((28 December 2012).dayOfWeek === Friday)
  }

  test("In ranges") {
    assert(((27 December 2012) in ((26 December 2012) -> (28 December 2012))) === true)
    assert(((27 December 2012) in ((27 December 2012) -> (28 December 2012))) === true)
    assert(((27 December 2012) in ((26 December 2012) -> (27 December 2012))) === true)
    assert(((27 December 2012) in ((27 December 2012) -> (27 December 2012))) === true)
    assert(((27 December 2012) in ((24 December 2012) -> (25 December 2012))) === false)
  }

  import org.joda.time.Interval
  test("Invalid range") {
    intercept[java.lang.IllegalArgumentException] {
      val interval: Interval = (26 March 2013) -> (26 March 2012)
    }
    intercept[java.lang.IllegalArgumentException] {
      val interval: Interval = (27 December 2012) -> (26 December 2012)
    }
  }

  test("WeekDays in range") {
    val days1 = Thursday in ((27 December 2012) -> (31 December 2012))
    assert(days1.length === 1)
    assert(days1(0) === (27 December 2012))

    val days2 = Thursday in ((1 November 2012) -> (30 November 2012))
    assert(days2.length === 5)
    assert(days2(0) === (1 November 2012))
    assert(days2(1) === (8 November 2012))
    assert(days2(2) === (15 November 2012))
    assert(days2(3) === (22 November 2012))
    assert(days2(4) === (29 November 2012))

    val days3 = Tuesday in ((27 December 2012) -> (9 January 2013))
    assert(days3.length === 2)
    assert(days3(0) === (1 January 2013))
    assert(days3(1) === (8 January 2013))

    val wholeYear = (1 January 2012) -> (31 December 2012)
    assert((Monday in wholeYear).length === 53)

  }
}
