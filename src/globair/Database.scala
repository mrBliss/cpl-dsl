package globair

trait DBFields {

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

  case class FlightCodeNumber(codeNumber: Int) extends IntField {
    require(codeNumber.toString matches "[0-9]{3,4}",
            "A flight code number must consist of 3 to 4 digits")
    val rep = codeNumber
  }

  case class FlightCode(ac: AirlineCode, fcn: FlightCodeNumber)
    extends StringField {
    override val rep = ac.toString + fcn.toString
  }
}

trait DBEntities {
  self: DBFields =>

  import DatabaseDSL._

  case class Country(name: String) extends Entity {
    def row = columns("name" -> name)
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
    airplaneModel: AirplaneModel) extends Entity {
    val row = columns("template" -> template, "date" -> date,
      "moment" -> moment, "airplaneModel" -> airplaneModel)
  }

  // TODO link with FlightTemplate?
  case class FlightMoment(template: FlightTemplate, weekday: WeekDay,
    time: Time) extends Entity {
    val row = columns("template" -> template, "weekday" -> weekday,
      "time" -> time)
  }

  case class AirplaneModel(name: String, maxNbOfSeats: Int, cruiseSpeed: Double,
    manufacturer: Manufacturer) extends Entity {
    val row = columns("maxNbOfSeats" -> maxNbOfSeats, "cruiseSpeed" -> cruiseSpeed,
      "manufacturer" -> manufacturer)
  }

  case class Manufacturer(name: String) extends Entity {
    val row = columns("name" -> name)
  }

  case class FlightTemplate(codeNumber: FlightCodeNumber,
    company: AirlineCompany, connection: Connection)
    extends Entity {

    lazy val flightCode: FlightCode = new FlightCode(company.code, codeNumber)

    val row = columns("codeNumber" -> codeNumber, "company" -> company,
      "connection" -> connection) // TODO
  }


}

class DB extends DBFields with DBEntities

trait FlightDSL extends DelayedInit {
  self: DB =>

  import collection.mutable.ListBuffer

  private val initCode = new ListBuffer[() => Unit]
  override def delayedInit(body: => Unit) {
    initCode += (() => body)
  }

  def run() {
    for (proc <- initCode) proc()

    println(countries)
    println(cities)
    println(airports)
    println(flightTemplates)
    println("DONE")
  }

  // Countries
  private var countries: List[Country] = Nil
  def country(name: String): Country = {
    val country = Country(name)
    countries ::= country
    country
  }
  // Cities
  private var cities: List[City] = Nil
  implicit def cityIn(cityName: String) = new {
    def in(country: Country) = {
      val city = new City(cityName, country)
      cities ::= city
      city
    }
  }
  // Airports
  private var airports: List[Airport] = Nil
  implicit def airportAt(pair: (String, String)) = new {
    def at(city: City): Airport = {
      val airport = new Airport(new AirportCode(pair._1), pair._2, city)
      airports ::= airport
      airport
    }
  }

  import globair.DatabaseDSL.PricingScheme
  // Airline Companies
  private var airlineCompanies: List[AirlineCompany] = Nil
  def company[SeatType <: DatabaseDSL.SeatType](code: String, name: String,
              pricingScheme: PricingScheme[AirlineCompany, SeatType]): AirlineCompany = {
    val company = new AirlineCompany(AirlineCode(code), name)
    airlineCompanies ::= company
    company
  }

  // Connections
  private var connections: List[Connection] = Nil

  // Flights
  implicit def dayRange(weekDay: WeekDay) = new {
    def during(range: (Date, Date)): Seq[Date] =
      range._1.weekDaysUntil(weekDay, range._2)
  }

  def every(weekDay: WeekDay): WeekDay = weekDay

  def at[SeatType <: DatabaseDSL.SeatType](time: Time, dates: Seq[Date])
                                          (seats: (SeatType, Int)*)
    = (time, dates, Map(seats: _*))

  def at[SeatType <: DatabaseDSL.SeatType](time: Time, date: Date)
                                          (seats: (SeatType, Int)*)
    = (time, List(date), Map(seats: _*))


  implicit def airplaneModelOf(airplaneModelName: String) = new {
    def of(company: Manufacturer) = new {
      def carries(passengers: Int) = new {
        def flies(speed: Int): AirplaneModel =
          new AirplaneModel(airplaneModelName, passengers, speed, company)
      }
    }
  }

  implicit def hourSyntax(hours: Int) = new {
    def h(minutes: Int) = Time(hours, minutes)
  }

  implicit def kmUnit(kms: Int) = new {
    def km: Double = kms
  }

  implicit def kmhUnit(kmhs: Int) = new {
    def kmh: Int = kmhs
  }

  implicit def pUnit(ps: Int) = new {
    def p: Int = ps
  }

  implicit def seatsSyntax(seats: Int) = new {
    def seats: Int = seats
  }

  private var flightTemplates: List[FlightTemplate] = Nil
  def FlightTemplate[SeatType <: DatabaseDSL.SeatType] (company: AirlineCompany, flightCode: Int)
                                                       (fromTo: (Airport, Airport), distance: Double)
                                                       (airplaneModel: AirplaneModel)
                                                       (tup: (Time, Seq[Date], Map[SeatType, Int])): FlightTemplate = {
    // TODO make the flights
    // TODO check if total number of seats <= airplaneModel.maxNbOfSeats
    val (from, to) = fromTo
    val (when, days, seats) = tup
    val conn = new Connection(from, to, distance)
    connections ::= conn
    val ft = new FlightTemplate(new FlightCodeNumber(flightCode), company, conn)
    flightTemplates ::= ft
    ft
  }
}

object Main extends App {

  Example.run()

}

object Main2 extends App with DBFields with DBEntities {
  import java.sql.DriverManager

  val c = new Country("Belgium")

  Class.forName("org.sqlite.JDBC");

  val conn = DriverManager.getConnection("jdbc:sqlite:test.db");

  val statement = conn.createStatement()
  statement.executeUpdate("DROP TABLE IF EXISTS Country");
  statement.executeUpdate("CREATE TABLE Country (id integer, name string)");

  conn.setAutoCommit(false)
  c.addToBatch(conn)
  conn.commit()

  val rs = statement.executeQuery("SELECT * FROM Country");

  while (rs.next()) {
    println("id = %d, name = %s, motto = %s" format (rs.getInt("id"),
      rs.getString("name")))
  }

  conn.close()

}
