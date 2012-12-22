package globair

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

object DatabaseDSL {

  import java.sql.Timestamp

  type ID = Int

  // class IDGenerator(start: ID = 0) {
  //   private var nextID = 0
  //   def next(): ID = {
  //     val id = nextID
  //     nextID += 1
  //     id
  //   }
  // }

  // TODO temp
  type Price = BigDecimal

  abstract class SeatType

  trait PricingScheme[Company, SeatType <: DatabaseDSL.SeatType] {

    // Type alias
    type PricingScheme = PartialFunction[(SeatType, Date, Price), Price]

    val defaultScheme: PricingScheme = {
      case (_, _, price) => price
    }

    implicit def andAlsoFunc(schemeA: PricingScheme) = new {
      def andAlso(schemeB: PricingScheme): PricingScheme =
        new PartialFunction[(SeatType, Date, Price), Price] {
          def isDefinedAt(x: (SeatType, Date, Price)): Boolean =
            schemeA.isDefinedAt(x) || schemeB.isDefinedAt(x)
          def apply(x: (SeatType, Date, Price)): Price =
              if (schemeA.isDefinedAt(x)) {
                if (schemeB.isDefinedAt(x))
                  schemeB.apply((x._1, x._2, schemeA.apply(x)))
                else
                  schemeA.apply(x)
              } else {
                schemeB.apply(x)
              }
        }
    }

    // Abstract, must be provided when defining a PricingScheme
    val scheme: PricingScheme
  }

  class Schedule[SeatType <: DatabaseDSL.SeatType](schedule: Seq[(Time, Date, Map[SeatType, Int])] = Vector()) {
    def at(time: Time, dates: Seq[Date])(seats: (SeatType, Int)*): Schedule[SeatType] = {
      val seatMap = Map(seats:_*)
      new Schedule(schedule ++ (dates map (date => (time, date, seatMap))))
    }

    def at(time: Time, date: Date)(seats: (SeatType, Int)*): Schedule[SeatType] =
      new Schedule(schedule :+ (time, date, Map(seats:_*)))

    def except(dates: Date*): Schedule[SeatType] =
      new Schedule(schedule filterNot (x => dates contains x._2))
  }


  trait Field[T] {
    def rep: T
    def fillIn(prepStat: PreparedStatement, i: Int): Unit
    override def toString = rep.toString
  }
  trait IntField extends Field[Int] {
    def fillIn(prepStat: PreparedStatement, i: Int) {
      prepStat.setInt(i, rep)
    }
  }
  trait BooleanField extends Field[Boolean] {
    def fillIn(prepStat: PreparedStatement, i: Int) {
      prepStat.setBoolean(i, rep)
    }
  }
  trait StringField extends Field[String] {
    def fillIn(prepStat: PreparedStatement, i: Int) {
      prepStat.setString(i, rep)
    }
  }
  trait DoubleField extends Field[Double] {
    def fillIn(prepStat: PreparedStatement, i: Int) {
      prepStat.setDouble(i, rep)
    }
  }
  trait DateField extends Field[Timestamp] {
    def fillIn(prepStat: PreparedStatement, i: Int) {
      prepStat.setTimestamp(i, rep)
    }
  }
  trait Entity {
    def row: Map[String, Field[_]]

    implicit def stringField(str: String) = new StringField { val rep = str }
    implicit def intField(n: Int) = new IntField { val rep = n }
    implicit def doubleField(d: Double) = new DoubleField { val rep = d }
    implicit def priceField(bd: Price) = new StringField { val rep = bd.toString } // TODO format
    implicit def timeField(t: Time) = new IntField { val rep = t.minutes * 60 + t.hours }
    implicit def dateField(d: Date) = new DateField { val rep = d.toTimestamp }
    implicit def foreignKeyField(e: Entity) = new IntField { val rep = e.id }

    protected def columns(cols: (String, Field[_])*): Map[String, Field[_]] = Map(cols: _*)

    def id: ID = 1 // TODO

    def tableName = this.getClass.getSimpleName
    def prepareStatement(conn: Connection): PreparedStatement = {
      val cols = row
      val prepStr = "INSERT INTO %s (%s) VALUES (%s)"
        .format(tableName,
                cols.map(_._1).mkString(", "),
                cols.map(_ => "?").mkString(", "))
      val stat = conn.prepareStatement(prepStr)
      cols.zipWithIndex.foreach { case ((col, field), i) => field.fillIn(stat, i + 1) }
      stat
    }
    def addToBatch(conn: Connection) {
      prepareStatement(conn).executeUpdate()
    }

  }

}
