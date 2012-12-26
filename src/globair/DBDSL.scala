
package globair

import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp}

object DBDSL {

  // TODO temp
  type Price = BigDecimal

  // Input: the name of the column;
  // Output: an sql statement that defines a column;
  // e.g.: "name" => "`name` VARCHAR(100) NULL DEFAULT NULL"
  type FieldGenerator = String => String

  trait Field[T] {
    def rep: T
    def mkStatement(output: SQLOutputFormat): FieldGenerator
    override def toString = rep.toString
  }
  trait IntField extends Field[Int] {
    def mkStatement(output: SQLOutputFormat) = output.int
  }
  trait StringField extends Field[String] {
    def mkStatement(output: SQLOutputFormat) = output.string()
  }
  trait DoubleField extends Field[Double] {
    def mkStatement(output: SQLOutputFormat) = output.double
  }
  trait DateTimeField extends Field[Timestamp] {
    def mkStatement(output: SQLOutputFormat) = output.dateTime
  }
  trait MoneyField extends Field[BigDecimal] {
    def mkStatement(output: SQLOutputFormat) = output.money
  }
  trait ForeignKeyField extends Field[Entity] {
    def mkStatement(output: SQLOutputFormat) = output.foreignKey(rep)
  }

  implicit def intField(n: Int) = new IntField { val rep = n }
  implicit def stringField(str: String) = new StringField { val rep = str }
  implicit def doubleField(d: Double) = new DoubleField { val rep = d }
  implicit def moneyField(bd: Price) = new MoneyField { val rep = bd }
  implicit def dateTimeField(d: DateTime) = new DateTimeField { val rep = d.toTimestamp }
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
    // def prepareStatement(conn: Connection): PreparedStatement = {
    //   val cols = row
    //   val prepStr = "INSERT INTO %s (%s) VALUES (%s)"
    //     .format(tableName,
    //             cols.map(_._1).mkString(", "),
    //             cols.map(_ => "?").mkString(", "))
    //   val stat = conn.prepareStatement(prepStr)
    //   cols.zipWithIndex.foreach { case ((col, field), i) => field.fillIn(stat, i + 1) }
    //   stat
    // }
    // def addToBatch(conn: Connection) {
    //   prepareStatement(conn).executeUpdate()
    // }

  }

}
