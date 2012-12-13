package globair
import scala.io.Source
import java.sql.{ Connection, DriverManager, ResultSet, SQLException, Statement }

object TableImport extends App {
	
	val importFileLoc = ".\\analysis\\database.sqlite"
	
	println("Importing table file from: " + System.getProperty("user.dir")
			+ importFileLoc.substring(1,importFileLoc.length()))
	val importData = Source.fromFile(importFileLoc).mkString 
	println(importData)
	
	 // Load the SQLite driver
  Class.forName("org.sqlite.JDBC");

  val conn = DriverManager.getConnection("jdbc:sqlite:test.db");
  val statement = conn.createStatement()
  statement.setQueryTimeout(30);

  statement.executeUpdate(importData);
  
  conn.close();
}