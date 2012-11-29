package globair

object Database {

  case class AirportCode(code: String) {
    require(code matches "[A-Z]{3}",
            "An airport code must consist of 3 capital letters")
  }
  
  case class AirlineCode(code: String) {
    require(code matches "[A-Z]{2,3}",
            "An airline code must consist of 2 to 3 capital letters")
  }

  case class FlightCodeNumber(codeNumber: String) {
    require(codeNumber matches "[0-9]{3,4}",
            "A flight code number must consist of 3 to 4 digits")
  }

  case class FlightCode(ac: AirlineCode, fcn: FlightCodeNumber) {
    override val toString = ac.toString + fcn.toString
  }

  object WeekDay extends Enumeration {
    type WeekDay = Value
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }
  import WeekDay._

  type Price = BigDecimal

  type ID = Int

  type Date = Int

  type Time = (Int, Int)

  case class Airport(code: AirportCode, name: String, city: City)

  case class City(id: ID, name: String, country: Country)
   
  case class Country(id: ID, name: String)
  
  case class Connection(from: Airport, to: Airport, distance: Double)
  
  case class FlightTemplate(codeNumber: FlightCodeNumber, company: AirlineCompany, connection: Connection) {
    val code: FlightCode = new FlightCode(company.code, codeNumber)
  }

  case class AirlineCompany(code: AirlineCode, name: String)

  case class SeatClass(id: ID, name: String)

  case class Seat(flight: Flight, number: Int, seatClass: SeatClass)

  case class Ticket(seat: Seat, price: Price)

  case class Flight(id: ID, template: FlightTemplate, date: Date, moment: FlightMoment)

  // TODO link with FlightTemplate?
  case class FlightMoment(template: FlightTemplate,  weekday:  WeekDay, time: Time)

  // TODO id was code
  case class Airplane(id: ID, model: AirplaneModel)

  case class AirplaneModel(id: ID, maxSeats: Int, maxSpeed: Double, manufacturer: Manufacturer)

  case class Manufacturer(id: ID, name: String)

}
