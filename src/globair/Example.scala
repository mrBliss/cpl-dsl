package globair

/**
 * Example usage of the DSL
 */
object Example extends FlightDSL with SQLitePopulator {
  import Date._

  val dbName = "jdbc:sqlite:test.db"

  // Countries
  val Belgium = country("Belgium")
  val France = country("France")
  val Germany = country("Germany")
  val Italy = country("Italy")
  val Netherlands = country("Netherlands")
  val Sweden = country("Sweden")
  val UK = country("United Kingdom")
  val US = country("United States")

  // Cities
  val Brussels = "Brussels" in Belgium
  val Paris = "Paris" in France
  val Frankfurt = "Frankfurt" in Germany
  val Rome = "Rome" in Italy
  val Amsterdam = "Amsterdam" in Netherlands
  val Stockholm = "Stockholm" in Sweden
  val London = "London" in UK
  val NewYork = "New York" in US

  // Airports
  val BRU = ("BRU", "Brussels Airport") at Brussels
  val CDG = ("CDG", "Charles de Gaulle Airport") at Paris
  val FRA = ("FRA", "Frankfurst am Main Airport") at Frankfurt
  val CIA = ("CIA", "Rome Ciampino Airport") at Rome
  val AMS = ("AMS", "Amsterdam Airport Schiphol") at Amsterdam
  val ARN = ("ARN", "Stockholm Arlanda Airport") at Stockholm
  val LHR = ("LHR", "London Heathrow Airport") at London
  val JFK = ("JFK", "John F. Kennedy International Airport") at NewYork

  // Manufacturers
  val Airbus = manufacturer("Airbus")
  val Boeing = manufacturer("Boeing")
  val Cessna = manufacturer("Cessna")

  // Airline Companies (the code is the official IATA code)
  val BM = company("BM", "British Midlands Airways", BMPricing)
  val SN = company("SN", "SN Brussels Airlines", null)
  val FR = company("FR", "Ryanair", null)
  val QF = company("QF", "Qantas Airways", null)
  val LH = company("LH", "Deutsche Lufthansa", null)

  // Airplane Models
  val AirbusA320 = "Airbus A320" of Airbus carries 150.p flies 828.kmh
  val AirbusA380 = "Airbus A380" of Airbus carries 644.p flies 945.kmh
  val Boeing727 = "Boeing 727" of Boeing carries 145.p flies 963.kmh
  val Boeing737_800 = "Boeing 737-800" of Boeing carries 160.p flies 828.kmh

  // One kind of seats
  case object SingleClass extends SeatKind

  // Two kinds of seats: Business and Economy
  sealed abstract class BusEcSeatTypes extends SeatKind
  case object Business extends BusEcSeatTypes
  case object Economy extends BusEcSeatTypes

  // Three kinds of seats: First, Second, and Third Class
  sealed abstract class NumericalSeatTypes extends SeatKind
  case object FirstClass extends NumericalSeatTypes
  case object SecondClass extends NumericalSeatTypes
  case object ThirdClass extends NumericalSeatTypes

  // Pricing Scheme of British Midlands
  val BMPricing = new PricingScheme[AirlineCompany, BusEcSeatTypes] {
    def isHighSeason(date: Date): Boolean =
      date.in(15 December 2012, 31 March 2012) ||
      date.in(1 July 2013, 31 August 2013) ||
      date.in(15 December 2013, 31 March 2014)

    val highSeason: PricingScheme = {
      case (Business, date, price) if isHighSeason(date) => price * 1.1
      case (Economy, date, price) if isHighSeason(date) => price * 1.05
    }
    val weekday: PricingScheme = {
      case (_, _, price) => price * 0.9
    }
    val holidays: PricingScheme = {
      // Christmas & Easter
      case (_, Date(25, December, _) | Date(31, March, _), price) => price * 2
      // Thanksgiving
      case (_, Date(22, November, _), price) => price * 1.5
      // Australia Day
      case (_, Date(26, January, _), price) => price * 1.2
    }
    val scheme = holidays orElse (weekday andAlso highSeason) orElse defaultScheme
  }

  // Flights

  val wholeYear = (1 January 2012) -> (31 December 2012)
  val summer = (21 June 2012) -> (21 September 2012)
/*
  FlightTemplate(BM, 1628)(BRU -> CDG, 757.km)(Boeing727) {
    new Schedule[BusEcSeatTypes]()
    .at(9 h 55, every(Monday) during wholeYear) {
      Business -> 25.seats;
      Economy -> 110.seats
    }
    .at(10 h 9, every(Friday) during summer) {
      Business -> 30.seats;
      Economy -> 115.seats
    }.except(25 December 2012, 6 January 2013)
    .at(15 h 3, 24 December 2012) {
      Business -> 20.seats;
      Economy -> 125.seats
    }
  }*/
}
