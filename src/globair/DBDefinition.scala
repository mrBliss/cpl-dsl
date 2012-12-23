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
