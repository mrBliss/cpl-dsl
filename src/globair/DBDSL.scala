
package globair

import java.sql.{Connection, DriverManager, PreparedStatement, Statement, Timestamp}
import collection.mutable.{Map => MutMap}


object DBDSL {

  // TODO temp
  type Price = BigDecimal

  trait Field[T] {
    def rep: T
    def fillIn(prepStat: PreparedStatement, i: Int)(implicit idMap: MutMap[Entity, ID]): Unit
    override def toString = rep.toString
  }
  trait IntField extends Field[Int] {
    def fillIn(prepStat: PreparedStatement, i: Int)(implicit idMap: MutMap[Entity, ID]) =
      prepStat.setInt(i, rep)
  }
  trait StringField extends Field[String] {
    def fillIn(prepStat: PreparedStatement, i: Int)(implicit idMap: MutMap[Entity, ID]) =
      prepStat.setString(i, rep)
  }
  trait DoubleField extends Field[Double] {
    def fillIn(prepStat: PreparedStatement, i: Int)(implicit idMap: MutMap[Entity, ID]) =
      prepStat.setDouble(i, rep)
  }
  trait TimestampField extends Field[Timestamp] {
    def fillIn(prepStat: PreparedStatement, i: Int)(implicit idMap: MutMap[Entity, ID]) =
      prepStat.setTimestamp(i, rep)
  }
  trait MoneyField extends Field[BigDecimal] {
    def fillIn(prepStat: PreparedStatement, i: Int)(implicit idMap: MutMap[Entity, ID]) =
      prepStat.setBigDecimal(i, rep.bigDecimal)
  }
  trait ForeignKeyField extends Field[Entity] {
    def fillIn(prepStat: PreparedStatement, i: Int)(implicit idMap: MutMap[Entity, ID]) =
      idMap get rep match {
        case Some(id) => prepStat.setInt(i, id)
        case None => stateError("No ID for entity " + rep)
      }
  }

  implicit def intField(n: Int) = new IntField { val rep = n }
  implicit def stringField(str: String) = new StringField { val rep = str }
  implicit def doubleField(d: Double) = new DoubleField { val rep = d }
  implicit def moneyField(bd: Price) = new MoneyField { val rep = bd }
  implicit def dateTimeField(d: DateTime) = new TimestampField { val rep = d.toTimestamp }
  implicit def foreignKeyField(e: Entity): Field[_] = new ForeignKeyField { val rep = e }


  // abstract class Key[F](val colName: String) extends Field[F]
  // case class StringKey(colName: String) extends Key[String](colName) {
  //   def rep: String =
  //   def mkStatement(output: SQLOutputFormat) = output.
  // }

  // TODO
  case class Key[F](val field: Field[F], val colName: String)
  // class AutoIncKey(val colName: String) extends Key[Nothing](new Field[Nothing] {
  //   def rep = null
  //   def mkStatement(
  //   }, colName)

  type ID = Int

  trait Entity {

    protected def columns(cols: (String, Field[_])*): Map[String, Field[_]] = Map(cols: _*)

    // TODO
    protected def autoInc(keyName: String): Key[Nothing] = null

    // TODO
    protected def useAsKey(keyName: String): Key[String] = null

    // TODO
    protected def unique(fields: String*): Unit = null

    def row: Map[String, Field[_]]

    def key: Key[_]

    def tableName = this.getClass.getSimpleName
    private def prepareStatement(conn: Connection)(implicit idMap: MutMap[Entity, ID]): PreparedStatement = {
      "INSERT INTO %s (%s) VALUES (%s)"
      val prepStr = "INSERT INTO %s (%s) VALUES (%s)"
        .format(tableName,
                row.map(_._1).mkString(", "),
                row.map(_ => "?").mkString(", "))
      println(prepStr)
      val stat = conn.prepareStatement(prepStr, Statement.RETURN_GENERATED_KEYS)
      row.zipWithIndex.foreach { case ((col, field), i) => field.fillIn(stat, i + 1) }
      stat
    }
    def insert(conn: Connection)(implicit idMap: MutMap[Entity, ID]): Option[ID] = {
      val stat = prepareStatement(conn)
      stat.executeUpdate()
      val genKeyResult = stat.getGeneratedKeys()
      if (genKeyResult.next()) Some(genKeyResult.getInt(1)) else None
    }

  }

}
