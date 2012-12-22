package object globair {
  type Year = Int
  type Day = Int

  type Hours = Int
  type Minutes = Int


  import org.joda.time.Days
  implicit def toDays(day: Day) = new {
    def days: Days = Days.days(day)
  }

}
