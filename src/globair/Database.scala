package globair

object Database {

  import DatabaseDSL._

  case class AirportCode(code: String) extends StringField {
    require(code matches "[A-Z]{3}",
            "An airport code must consist of 3 capital letters")
    val rep = code
  }

  case class AirlineCode(code: String) extends StringField {
    require(code matches "[A-Z]{2,3}",
            "An airline code must consist of 2 to 3 capital letters")
    val rep = code
  }

  case class FlightCodeNumber(codeNumber: String) extends StringField {
    require(codeNumber matches "[0-9]{3,4}",
            "A flight code number must consist of 3 to 4 digits")
    val rep = codeNumber
  }

  case class FlightCode(ac: AirlineCode, fcn: FlightCodeNumber)
       extends StringField {
    override val rep = ac.toString + fcn.toString
  }

  case class Country(name: String, motto: String) extends Entity {
    def row = columns("name" -> name, "motto" -> motto)
  }

  case class City(name: String, country: Country) extends Entity {
    def row = columns("name" -> name, "country" -> country)
  }

  case class Airport(code: AirportCode, name: String, city: City)
       extends Entity {
    def row = columns("code" -> code, "name" -> name, "city" -> city)
  }

  case class AirlineCompany(code: AirlineCode, name: String) extends Entity {
    def row = columns("code" -> code, "name" -> name)
  }

  object AirlineCompany {
    def apply(companyCode: String): AirlineCompany = sys.error("Not implemented")
  }

  case class Connection(from: Airport, to: Airport, distance: Double)
       extends Entity {
    val row = columns("from" -> from, "to" -> to, "distance" -> distance)
  }

  case class SeatClass(name: String, company: AirlineCompany) extends Entity {
    val row = columns("name" -> name, "company" -> company)
  }

  case class Seat(flight: Flight, number: Int, seatClass: SeatClass)
       extends Entity {
    val row = columns("flight" -> flight, "number" -> number,
                      "seatClass" -> seatClass)
  }

  case class Ticket(seat: Seat, price: Price) extends Entity {
    val row = columns("seat" -> seat, "price" -> price)
  }

  case class Flight(template: FlightTemplate, date: Date, moment: FlightMoment,
                    airplane: Airplane) extends Entity {
    val row = columns("template" -> template, "date" -> date,
                      "moment" -> moment, "airplane" -> airplane)
  }

  // TODO link with FlightTemplate?
  case class FlightMoment(template: FlightTemplate, weekday: WeekDay,
                          time: Time) extends Entity {
    val row = columns("template" -> template, "weekday" -> weekday,
                      "time" -> time)
  }

  // TODO id was code
  case class Airplane(model: AirplaneModel) extends Entity {
    val row = columns("model" -> model)
  }

  case class AirplaneModel(maxNbOfSeats: Int, maxSpeed: Double,
                           manufacturer: Manufacturer) extends Entity {
    val row = columns("maxNbOfSeats" -> maxNbOfSeats, "maxSpeed" -> maxSpeed,
                      "manufacturer" -> manufacturer)
  }

  case class Manufacturer(name: String) extends Entity {
    val row = columns("name" -> name)
  }

  case class FlightTemplate(codeNumber: FlightCodeNumber,
                            company: AirlineCompany, connection: Connection)
       extends Entity {

    def this(codeNumberStr: String, companyCodeStr: String, conn: Connection) =
      this(new FlightCodeNumber(codeNumberStr), AirlineCompany(companyCodeStr), conn)

    lazy val flightCode: FlightCode = new FlightCode(company.code, codeNumber)

    val row = columns() // TODO
  }

}

object Main extends App {
  import java.sql.DriverManager
  import Database._

  val c = new Country("Belgium", "Boop")

  Class.forName("org.sqlite.JDBC");

  val conn = DriverManager.getConnection("jdbc:sqlite:test.db");

  val statement = conn.createStatement()
  statement.executeUpdate("DROP TABLE IF EXISTS Country");
  statement.executeUpdate("CREATE TABLE Country (id integer, name string, motto string)");

  conn.setAutoCommit(false)
  c.addToBatch(conn)
  conn.commit()


  val rs = statement.executeQuery("SELECT * FROM Country");

  while (rs.next()) {
    println("id = %d, name = %s, motto = %s" format (rs.getInt("id"),
                                                     rs.getString("name"),
                                                     rs.getString("motto")))
  }

  conn.close()

}
