package globair

/**
 * Example usage of the DSL
 */
// object Example extends FlightDSL with SQLitePopulator {
object Example extends FlightDSL with MySQLPopulator {
  import Date._
  import Month._
  import WeekDay._

  // val dbName = "jdbc:sqlite:test.db"
  val dbName = "jdbc:mysql://localhost/cpl?user=cpl&password=clarke"

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

  // Airplane Models
  val AirbusA320 = "Airbus A320" of Airbus carries 150.p flies 828.kmh
  val AirbusA380 = "Airbus A380" of Airbus carries 644.p flies 945.kmh
  val Boeing727 = "Boeing 727" of Boeing carries 145.p flies 963.kmh
  val Boeing737_800 = "Boeing 737-800" of Boeing carries 160.p flies 828.kmh

  // Two kinds of seats: Business and Economy
  val Business = seatType("Business")
  val Economy = seatType("Economy")

  // Three kinds of seats: First, Second, and Third Class
  val FirstClass = seatType("First Class")
  val SecondClass = seatType("Second Class")
  val ThirdClass = seatType("Third Class")

  // Airline Companies (the code is the official IATA code)
  val BM = company("BM", "British Midlands Airways", new PricingScheme {

    def isHighSeason(date: Date): Boolean =
      date.in(15 December 2012, 31 March 2013) ||
      date.in(1 July 2013, 31 August 2013) ||
      date.in(15 December 2013, 31 March 2014)

    val highSeason: PricingScheme = {
      case (Business, date, price) if isHighSeason(date) => price * 1.1
      case (Economy, date, price) if isHighSeason(date) => price * 1.05
    }

    val scheme = highSeason
  })

  val SN = company("SN", "SN Brussels Airlines", new PricingScheme {

    def isHighSeason(date: Date): Boolean =
      date.in(1 December 2012, 15 March 2013) ||
      date.in(1 July 2013, 30 September 2013) ||
      date.in(1 December 2013, 15 March 2014)

    val highSeason: PricingScheme = {
      case (_, date, price) if isHighSeason(date) => price * 1.1
    }

    val specialActionOnSecondClass: PricingScheme = {
      case (SecondClass, date, price) if date.in(15 July 2012, 15 August 2012) => price - 50.EUR
    }

    val backToSchool: PricingScheme = {
      case (ThirdClass, date, price) if date.in(15 August 2012, 31 August 2012) => price - 10.EUR
    }

    val scheme = highSeason andAlso specialActionOnSecondClass
  })



  // Flights

  val wholeYear = (1 January 2012) -> (31 December 2012)
  val summer = (21 June 2012) -> (21 September 2012)

  val wholeWeek = Monday -> Sunday

  // British Midlands
  FlightTemplate(BM, 1628)(BRU -> CDG, 757.km)(Boeing727) {
    new Schedule()
      .at(9 h 55, every(Monday) during wholeYear) {
      Business -> (25.seats at 250.EUR);
      Economy -> (110.seats at 150.EUR)
    }.at(10 h 9, every(Friday) during summer) {
      Business -> (30.seats at 300.EUR);
      Economy -> (115.seats at 200.EUR)
    }.except(25 December 2012, 6 January 2013)
      .at(15 h 3, 24 December 2012) {
      Business -> (20.seats at 400.EUR);
      Economy -> (125.seats at 300.EUR)
    }
  }

  FlightTemplate(BM, 2439)(FRA -> ARN, 1227.km)(Boeing737_800) {
    new Schedule()
      .at(4 h 23, every(Saturday) during wholeYear) {
      Business -> (40.seats at 350.EUR);
      Economy -> (120.seats at 250.EUR)
    }
  }


  // SN Brussels Airlines
  FlightTemplate(SN, 324)(BRU -> CIA, 1187.km)(AirbusA380) {
    new Schedule()
      .at(21 h 40, wholeWeek during summer) {
      FirstClass -> (42.seats at 600.EUR);
      SecondClass -> (102.seats at 325.EUR)
      ThirdClass -> (500.seats at 287.EUR)
    }
  }
}
