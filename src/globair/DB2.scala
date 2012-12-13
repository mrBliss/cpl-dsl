package globair

import java.sql.Connection
import java.sql.PreparedStatement

object DB2 {

  class Table[T] {
    // def column[A](fieldName: String) = null
    // def link[A](fieldName: String) = null
  }

  type ID = Int

  class AirportCode(code: String) extends Field {
    require(code matches "[A-Z]{3}",
      "An airport code must consist of 3 capital letters")
    val rep = code
  }

  class AirlineCode(code: String) extends Field {
    require(code matches "[A-Z]{2,3}",
      "An airline code must consist of 2 to 3 capital letters")
    val rep = code
  }

  class FlightCodeNumber(codeNumber: String) extends Field {
    require(codeNumber matches "[0-9]{3,4}",
      "A flight code number must consist of 3 to 4 digits")
    val rep = codeNumber
  }

  class FlightCode(ac: AirlineCode, fcn: FlightCodeNumber) extends Field {
    override val rep = ac.toString + fcn.toString
  }

  // class Country extends Table[(ID, String)] {
  //   def id = column[ID]("id")
  //   def name = column[String]("name")
  // }
  // object Country {
  //   def apply(id: ID, name: String)
  // }

  // class City extends Table[(ID, String, Country)] {
  //   def id = column[ID]("id")
  //   def name = column[String]("name")
  //   def country = link[Country]("country")
  // }

  // class Airport extends Table[(AirportCode, String, City)] {
  //   def code = column[AirportCode]("code")
  //   def name = column[String]("name")
  //   def city = column[City]("city")
  // }

  trait Field {
    def rep: String
    override def toString = rep
  }

  trait Entity {
    def tableName = this.getClass.getSimpleName
    def prepareStatement(conn: Connection): PreparedStatement = {
      val columns = row
      val prepStr = "INSERT INTO %s values(%s)"
        .format(tableName, columns.map(_ => "?").mkString(", "))
      val stat = conn.prepareStatement(prepStr)
      // CONTINUE with filling in the fields
      stat
    }
    implicit def stringField(str: String) = new Field { def rep = str }
    implicit def intField(n: Int) = new Field { val rep = n.toString }

    def row: List[Field]
  }

  class Country(val id: ID, val name: String, val motto: String) extends Entity {
    def row = List(id, name, motto)
    // statement.executeUpdate("insert into person values(1, 'leo')");
  }

}
object Main extends App {
  import DB2._

  val c = new Country(1, "Belgium", "Boop")

}
