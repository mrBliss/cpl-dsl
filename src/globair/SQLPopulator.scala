package globair

import java.sql.{DriverManager, Connection}
import DBDSL.{Entity, IDMap}

trait SQLPopulator {

  // The only method to implement
  def connect(jdbcString: String): Connection


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
    entity.insert(conn, idMap) match {
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
}
