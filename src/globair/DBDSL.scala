
package globair

import java.sql.{Connection, DriverManager, PreparedStatement, Statement, Timestamp}


object DBDSL {

  // TODO temp
  type Price = BigDecimal

  trait Field[T] {
    def repr: T
    // TODO find a way to remove idMap from this signature
    def fillIn(prepStat: PreparedStatement, i: Int, idMap: IDMap): Unit
    override def toString = repr.toString
  }
  trait IntField extends Field[Int] {
    def fillIn(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setInt(i, repr)
  }
  trait StringField extends Field[String] {
    def fillIn(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setString(i, repr)
  }
  trait DoubleField extends Field[Double] {
    def fillIn(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setDouble(i, repr)
  }
  trait TimestampField extends Field[Timestamp] {
    def fillIn(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setTimestamp(i, repr)
  }
  trait MoneyField extends Field[BigDecimal] {
    def fillIn(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setBigDecimal(i, repr.bigDecimal)
  }
  trait ForeignKeyField extends Field[Entity] {
    def fillIn(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      idMap get repr match {
        case Some(id) => prepStat.setInt(i, id)
        case None => stateError("No ID for entity " + repr)
      }
  }

  implicit def intField(n: Int) = new IntField { val repr = n }
  implicit def stringField(str: String) = new StringField { val repr = str }
  implicit def doubleField(d: Double) = new DoubleField { val repr = d }
  implicit def moneyField(bd: Price) = new MoneyField { val repr = bd }
  implicit def dateTimeField(d: DateTime) = new TimestampField { val repr = d.toTimestamp }
  implicit def foreignKeyField(e: Entity): Field[_] = new ForeignKeyField { val repr = e }


  // abstract class Key[F](val colName: String) extends Field[F]
  // case class StringKey(colName: String) extends Key[String](colName) {
  //   def repr: String =
  //   def mkStatement(output: SQLOutputFormat) = output.
  // }

  // TODO
  case class Key[F](val field: Field[F], val colName: String)
  // class AutoIncKey(val colName: String) extends Key[Nothing](new Field[Nothing] {
  //   def repr = null
  //   def mkStatement(
  //   }, colName)

  type ID = Int

  type IDMap = Map[Entity, ID]

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
    private def prepareStatement(conn: Connection, idMap: IDMap): PreparedStatement = {
      "INSERT INTO %s (%s) VALUES (%s)"
      val prepStr = "INSERT INTO %s (%s) VALUES (%s)"
        .format(tableName,
                row.map(_._1).mkString(", "),
                row.map(_ => "?").mkString(", "))
      println(prepStr)
      val stat = conn.prepareStatement(prepStr, Statement.RETURN_GENERATED_KEYS)
      row.zipWithIndex.foreach { case ((col, field), i) => field.fillIn(stat, i + 1, idMap) }
      stat
    }
    def insert(conn: Connection, idMap: IDMap): Option[ID] = {
      val stat = prepareStatement(conn, idMap)
      stat.executeUpdate()
      val genKeyResult = stat.getGeneratedKeys()
      if (genKeyResult.next()) Some(genKeyResult.getInt(1)) else None
    }

  }

}
