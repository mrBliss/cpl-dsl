package globair

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

object DatabaseDSL {

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
  type Time = (Int, Int)

  // TODO temp
  type Date = Int

  // TODO temp

  type Price = BigDecimal

  sealed abstract class WeekDay(val ord: Int) extends IntField {
      def rep = ord
  }
  case object Monday extends WeekDay(0)
  case object Tuesday extends WeekDay(1)
  case object Wednesday extends WeekDay(2)
  case object Thursday extends WeekDay(3)
  case object Friday extends WeekDay(4)
  case object Saturday extends WeekDay(5)
  case object Sunday extends WeekDay(6)

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
  trait Entity {
    def row: Map[String, Field[_]]

    implicit def stringField(str: String) = new StringField { val rep = str }
    implicit def intField(n: Int) = new IntField { val rep = n }
    implicit def doubleField(d: Double) = new DoubleField { val rep = d }
    implicit def priceField(bd: Price) = new StringField { val rep = bd.toString } // TODO format
    implicit def timeField(t: Time) = new IntField { val rep = t._1 * 60 + t._2 }
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
