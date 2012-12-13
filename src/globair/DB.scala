package globair

object Database {

  object WeekDay extends Enumeration {
    type WeekDay = Value
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }
  import WeekDay._

  type Price = BigDecimal

  type Date = Int

  type Time = (Int, Int)

  case class ID(val id: Int)

  class Entity

  case class AirportCode(code: String) extends Entity {
    require(code matches "[A-Z]{3}",
      "An airport code must consist of 3 capital letters")
  }

  case class AirlineCode(code: String) extends Entity {
    require(code matches "[A-Z]{2,3}",
      "An airline code must consist of 2 to 3 capital letters")
  }

  case class FlightCodeNumber(codeNumber: String) extends Entity {
    require(codeNumber matches "[0-9]{3,4}",
      "A flight code number must consist of 3 to 4 digits")
  }

  case class FlightCode(ac: AirlineCode, fcn: FlightCodeNumber) extends Entity {
    override val toString = ac.toString + fcn.toString
  }

  case class Airport(code: AirportCode, name: String, city: City) extends Entity

  case class City(id: ID, name: String, country: Country) extends Entity

  case class Country(id: ID, name: String) extends Entity

  case class Connection(from: Airport, to: Airport, distance: Double)
    extends Entity

  case class FlightTemplate(codeNumber: FlightCodeNumber,
    company: AirlineCompany, connection: Connection) extends Entity {
    val code: FlightCode = new FlightCode(company.code, codeNumber)
  }

  case class AirlineCompany(code: AirlineCode, name: String) extends Entity

  case class SeatClass(id: ID, name: String, company: AirlineCompany)
    extends Entity

  case class Seat(flight: Flight, number: Int, seatClass: SeatClass)
    extends Entity

  case class Ticket(seat: Seat, price: Price) extends Entity

  case class Flight(id: ID, template: FlightTemplate, date: Date,
    moment: FlightMoment, airplane: Airplane) extends Entity

  // TODO link with FlightTemplate?
  case class FlightMoment(template: FlightTemplate, weekday: WeekDay,
    time: Time) extends Entity

  // TODO id was code
  case class Airplane(id: ID, model: AirplaneModel) extends Entity

  case class AirplaneModel(id: ID, maxNbOfSeats: Int, maxSpeed: Double,
    manufacturer: Manufacturer) extends Entity

  case class Manufacturer(id: ID, name: String) extends Entity

}
