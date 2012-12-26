package globair

/**
 * Definition of the database tables
 * DBFields: custom field types that can be used by the tables
 * DBEntities: the database tables
 */

trait DBFields {

  import DBDSL._

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

  import DBDSL._

  case class Country(name: String) extends Entity {
    val key = autoInc("id")
    val row = columns("name" -> name)
  }

  case class City(name: String, country: Country) extends Entity {
    val key = autoInc("id")
    val row = columns("name" -> name, "id_Country" -> country)
  }

  case class Airport(code: AirportCode, name: String, city: City)
    extends Entity {
    val key = Key(code)
    val row = columns("code" -> code, "name" -> name, "id_City" -> city)
  }

  case class AirlineCompany(code: AirlineCode, name: String) extends Entity {
    val key = Key(code)
    val row = columns("code" -> code, "name" -> name)
  }

  case class Connection(from: Airport, to: Airport, distance: Double)
    extends Entity {
    val key = autoInc("id")
    val row = columns("code_fromAirport" -> from, "code_toAirport" -> to, "distance" -> distance)
    unique("code_fromAirport", "code_toAirport")
  }

  case class SeatType(name: String) extends Entity {
    val key = autoInc("id")
    val row = columns("name" -> name)
  }
  case class SeatPricing(seatType: SeatType, flight: Flight, price: Price, nbSeats: Int)
       extends Entity {
    val key = autoInc("id")
    val row = columns("id_SeatType" -> seatType, "id_Flight" -> flight,
                      "price" -> price, "nbSeats" -> nbSeats)
    unique("id_SeatType", "id_Flight")
  }

  // case class Seat(flight: Flight, number: Int, seatClass: SeatClass)
  //   extends Entity {
  //   val row = columns("flight" -> flight, "number" -> number,
  //     "seatClass" -> seatClass)
  // }

  // case class Ticket(seat: Seat, price: Price) extends Entity {
  //   val row = columns("seat" -> seat, "price" -> price)
  // }

  // TODO DateTime
  case class Flight(template: FlightTemplate, date: Date, time: Time,
    airplaneModel: AirplaneModel) extends Entity {
    val key = autoInc("id")
    val row = columns("id_FlightTemplate" -> template, "time" -> date,
                      "code_AirplaneModel" -> airplaneModel)
    unique("time")
  }

  // // TODO link with FlightTemplate?
  // case class FlightMoment(template: FlightTemplate, weekday: WeekDay,
  //   time: Time) extends Entity {
  //   val row = columns("template" -> template, "weekday" -> weekday,
  //     "time" -> time)
  // }

  case class AirplaneModel(name: String, cruiseSpeed: Double, maxNbOfSeats: Int,
                           manufacturer: Manufacturer) extends Entity {
    val key = autoInc("id")
    val row = columns("name" -> name, "cruiseSpeed" -> cruiseSpeed, "maxNbOfSeats" -> maxNbOfSeats,
                      "id_Manufacturer" -> manufacturer)
  }

  case class Manufacturer(name: String) extends Entity {
    val key = autoInc("id")
    val row = columns("name" -> name)
  }

  case class FlightTemplate(code: FlightCodeNumber,
    company: AirlineCompany, connection: Connection)
    extends Entity {

    lazy val flightCode: FlightCode = new FlightCode(company.code, code)

    val key = autoInc("id")

    val row = columns("code" -> code, "code_AirlineCompany" -> company,
                      "id_Connection" -> connection)

    unique("code", "code_AirlineCompany")
  }

}

trait DBDefinition extends DBFields with DBEntities