package globair
import scala.io.Source
import java.sql.{ Connection, DriverManager, ResultSet, SQLException, Statement }

object TableImport extends App {

  val importFileLoc = "analysis/database.sql"

  println("Importing table file from: " + System.getProperty("user.dir")
    + importFileLoc.substring(1, importFileLoc.length()))
  val importData = Source.fromFile(importFileLoc).mkString
  // println(importData)

  var queries = importData.split(";");

  // Load the MySQL driver
  Class.forName("com.mysql.jdbc.Driver");

  val conn = DriverManager.getConnection("jdbc:mysql://localhost/cpl?user=cpl&password=clarke");
  val statement = conn.createStatement()
  statement.setQueryTimeout(30);
  for (query <- queries) {
    println(query)
    statement.executeUpdate(query)
  }
  //statement.executeUpdate(importData);

  conn.close();
}
