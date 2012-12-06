package globair

import java.sql.{ Connection, DriverManager, ResultSet, SQLException, Statement }

object SQLite extends App {

  // Load the SQLite driver
  Class.forName("org.sqlite.JDBC");

  val conn = DriverManager.getConnection("jdbc:sqlite:test.db");
  val statement = conn.createStatement()
  statement.setQueryTimeout(30);

  statement.executeUpdate("DROP TABLE IF EXISTS person");
  statement.executeUpdate("create table person (id integer, name string)");
  statement.executeUpdate("insert into person values(1, 'leo')");
  statement.executeUpdate("insert into person values(2, 'yui')");
  val rs = statement.executeQuery("select * from person");

  while (rs.next()) {
    println("name = %s, id = %d" format (rs.getString("name"), rs.getInt("id")))
  }

  conn.close()

}

