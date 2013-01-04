package globair
import scala.io.Source
import java.sql.{ Connection, DriverManager, ResultSet, SQLException, Statement }

object TableImport extends App {

  val importFileLoc = "analysis/database.sqlite"

  println("Importing table file from: " + importFileLoc)
  val importData = Source.fromFile(importFileLoc).mkString

  var queries = importData.split(";");

  // Load the SQLite driver
  Class.forName("org.sqlite.JDBC");

  val conn = DriverManager.getConnection("jdbc:sqlite:test.db");
  val statement = conn.createStatement()
  statement.setQueryTimeout(30);
  for (query <- queries) {
    statement.executeUpdate(query)
  }

  conn.close();
}
