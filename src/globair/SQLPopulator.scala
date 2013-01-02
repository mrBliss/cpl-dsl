package globair

import java.sql.{DriverManager, Connection, PreparedStatement, Timestamp}
import DBDSL.{Entity, IDMap}

trait SQLDataTypeMapper {

  def apply(x: Int)(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setInt(i, x)

  def apply(s: String)(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setString(i, s)

  def apply(d: Double)(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setDouble(i, d)

  def apply(t: Timestamp)(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setTimestamp(i, t)

  def apply(bd: BigDecimal)(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setBigDecimal(i, bd.bigDecimal)

  def apply(entity: Entity)(prepStat: PreparedStatement, i: Int, idMap: IDMap) = entity.key match {
    case Some(keyName) => entity.row(keyName).prep(this)(prepStat, i, idMap)
    case None => idMap get entity match {
      case Some(id) => prepStat.setInt(i, id)
      case None => stateError("No ID for entity " + entity)
    }
  }
}

trait SQLPopulator {

  def connect(jdbcString: String): Connection


  // Override this one for a custom SQLDataTypeMapper
  val dataTypeMapper: SQLDataTypeMapper = new SQLDataTypeMapper() {}

  // The important method
  final def populate(jdbcString: String)
                    (entitiesSeq: Seq[Entity]*): Unit = {
    using(jdbcString) { conn =>
      ((Map(): IDMap) /: entitiesSeq.flatten)(insertAndSaveID(conn))
    }
  }

  // Private helper to make the fold *more* readable
  private def insertAndSaveID(conn: Connection)(idMap: IDMap, entity: Entity): IDMap = {
    println(entity)
    entity.insert(conn, dataTypeMapper, idMap) match {
      case Some(id) => idMap + (entity -> id)
      case None => idMap
    }
  }

  // Private helper that implements the Loan pattern
  private def using(jdbcString: String)(body: Connection => Unit) {
    val conn = connect(jdbcString)
    try {
      body(conn)
    } finally {
      conn.close()
    }
  }

}

trait SQLitePopulator extends SQLPopulator {
  def connect(jdbcString: String): Connection = {
    Class.forName("org.sqlite.JDBC")
    DriverManager.getConnection(jdbcString)
  }

  override val dataTypeMapper = new SQLDataTypeMapper {
    override def apply(bd: BigDecimal)(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setString(i, bd.toString)
  }
}
