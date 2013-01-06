
package globair

import java.sql.{Connection, DriverManager, PreparedStatement, Statement, Timestamp}

/**
 * A tiny DSL used to define DBDefinition.
 * General concepts like Field and Entity are defined here.
 */

object DBDSL {

  // TODO temp
  type Price = BigDecimal

  /**
   * Functions that have this type are likely to fill in an indexed
   * field of the PreparedStatement with a value of a certain type.
   * The difficulty lies in the fact that this isn't done by an
   * overloaded method, but with `setString(..)`, `setInt(..)`, etc.
   * The final argument is necessary to fill in the ID of other rows,
   * in the case of foreign keys.
   */
  type Prep = (PreparedStatement, Int, IDMap) => Unit

  /**
   * A row in a table or Entity exists out of Fields.
   */
  trait Field[T] {
    // The represented type
    def repr: T
    // Delegates the filling in of a PreparedStatement to the
    // SQLDataTypeMapper.
    def prep(dataMapper: SQLDataTypeMapper): Prep
    override def toString = repr.toString
  }
  // Predefined fields.
  trait IntField extends Field[Int] {
    // We have to repeat this line in every field, because we are
    // dispatching on the first argument (repr: T) of the
    // SQLDataTypeMapper function call.
    def prep(dataMapper: SQLDataTypeMapper): Prep = dataMapper(repr) _
  }
  trait StringField extends Field[String] {
    def prep(dataMapper: SQLDataTypeMapper): Prep = dataMapper(repr) _
  }
  trait DoubleField extends Field[Double] {
    def prep(dataMapper: SQLDataTypeMapper): Prep = dataMapper(repr) _
  }
  trait TimestampField extends Field[Timestamp] {
    def prep(dataMapper: SQLDataTypeMapper): Prep = dataMapper(repr) _
  }
  trait MoneyField extends Field[BigDecimal] {
    def prep(dataMapper: SQLDataTypeMapper): Prep = dataMapper(repr) _
  }
  trait ForeignKeyField extends Field[Entity] {
    def prep(dataMapper: SQLDataTypeMapper): Prep = dataMapper(repr) _
  }

  // Implicits that convert basic types to fields, these are applied
  // in the calls to `columns` in DBEntities.
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

  // A table in a database
  trait Entity {

    // Abstract fields

    // A map from field names to fields values, used for populating an
    // actual table row in the database.
    def row: Map[String, Field[_]]

    // def key: Key[_] TODO
    def key: Option[String]



    // Helper function for defining `row`
    protected def columns(cols: (String, Field[_])*): Map[String, Field[_]] = Map(cols: _*)

    // TODO
    protected def autoInc(keyName: String): Option[String] = None

    // TODO
    protected def useAsKey(keyName: String): Option[String] = Some(keyName)

    // TODO
    protected def unique(fields: String*): Unit = null


    def tableName = this.getClass.getSimpleName

    // Generate a PreparedStatement and fill it in with `row` and the SQLDataTypeMapper.
    private def prepareStatement(conn: Connection, dataTypeMapper: SQLDataTypeMapper, idMap: IDMap): PreparedStatement = {
      // NOTE: We are not generating SQL Strings by hand! We are
      // generating the string for the PreparedStatement that looks
      // like "INSERT INTO tableName (col1, col2, ...) VALUES (?, ?, ...)",
      // The question marks are filled in by the actual values.
      "INSERT INTO %s (%s) VALUES (%s)"
      val prepStr = "INSERT INTO %s (%s) VALUES (%s)"
        .format(tableName,
                row.map(_._1).mkString(", "),
                row.map(_ => "?").mkString(", "))
      println(prepStr) // TODO
      // The second argument indicates that we wish to receive the auto-generated keys
      val stat = conn.prepareStatement(prepStr, Statement.RETURN_GENERATED_KEYS)
      // Filling in the fields
      row.zipWithIndex.foreach { case ((col, field), i) => field.prep(dataTypeMapper)(stat, i + 1, idMap) }
      stat
    }

    // Insert this table row or Entity into the database, return the
    // auto-generated key if there was one.
    def insert(conn: Connection, dataTypeMapper: SQLDataTypeMapper, idMap: IDMap): Option[ID] = {
      val stat = prepareStatement(conn, dataTypeMapper, idMap)
      stat.executeUpdate()
      val genKeyResult = stat.getGeneratedKeys()
      if (genKeyResult.next()) Some(genKeyResult.getInt(1)) else None
    }

  }

}
