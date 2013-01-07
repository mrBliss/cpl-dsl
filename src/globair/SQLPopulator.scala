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
    // A field of the Entity is used as a key
    case Some(keyName) => entity.row(keyName).prep(this)(prepStat, i, idMap)
    // The Entity has an auto-generated key, which should be stored in the idMap
    case None => idMap get entity match {
      case Some(id) => prepStat.setInt(i, id)
      case None => stateError("No ID for entity " + entity)
    }
  }
}

trait SQLPopulator {

  // Should load the JDBC driver and return a connection for the given
  // JDBC String.
  def connect(jdbcString: String): Connection

  // Override this one for a custom database skeleton. Should be the
  // name of a file present in the resources folder.
  val dbSkeleton = "/database.sql"

  // Override this one for a custom SQLDataTypeMapper
  val dataTypeMapper: SQLDataTypeMapper = new SQLDataTypeMapper() {}

  // The important method
  final def populate(jdbcString: String)
                    (entitiesSeq: Seq[Entity]*): Unit = {
    val allEntities = entitiesSeq.flatten
    using(jdbcString) { conn =>
      ((Map(): IDMap) /: allEntities)(insertAndSaveID(conn))
      println("Successfully saved %d entities" format allEntities.length)
    }
  }

  // Drop and reimport the database tables defined in dbSkeleton
  final def prepareDatabase(jdbcString: String): Unit =
    Option(getClass getResource dbSkeleton) match {
      case None => stateError("Missing dbSkeleton: " + dbSkeleton)
      case Some(res) => {
        val statements = scala.io.Source.fromURL(res).mkString.split(";")
        using(jdbcString) { conn =>
          val statement = conn.createStatement()
          statement.setQueryTimeout(30);
          for (stat <- statements)
          statement executeUpdate stat
        }
      }
    }


  // Private helper to make the fold in `populate` *more* readable
  private def insertAndSaveID(conn: Connection)(idMap: IDMap, entity: Entity): IDMap = {
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

  // SQLite requires a custom dbSkeleton
  override val dbSkeleton = "/database.sqlite"

  override val dataTypeMapper = new SQLDataTypeMapper {
    // SQLite doesn't support setBigDecimal(..)p
    override def apply(bd: BigDecimal)(prepStat: PreparedStatement, i: Int, idMap: IDMap) =
      prepStat.setString(i, bd.toString)
  }
}

trait MySQLPopulator extends SQLPopulator {

  def connect(jdbcString: String): Connection = {
    Class.forName("com.mysql.jdbc.Driver")
    DriverManager.getConnection(jdbcString)
  }

}
