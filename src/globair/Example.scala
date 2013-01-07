package globair

/**
 * Example usage of the DSL
 */
object Example extends FlightDSL with SQLitePopulator {
  import Date._
  import Month._
  import WeekDay._

  val dbName = "jdbc:sqlite:test.db"

  // Countries
  val Belgium = country("Belgium")
  val France = country("France")
  val Germany = country("Germany")
  val Italy = country("Italy")
  val Sweden = country("Sweden")

  // Cities
  val Brussels = "Brussels" in Belgium
  val Paris = "Paris" in France
  val Frankfurt = "Frankfurt" in Germany
  val Rome = "Rome" in Italy
  val Stockholm = "Stockholm" in Sweden

  // Airports
  val BRU = ("BRU", "Brussels Airport") at Brussels
  val CDG = ("CDG", "Charles de Gaulle Airport") at Paris
  val FRA = ("FRA", "Frankfurt am Main Airport") at Frankfurt
  val CIA = ("CIA", "Rome Ciampino Airport") at Rome
  val ARN = ("ARN", "Stockholm Arlanda Airport") at Stockholm

  // Manufacturers
  val Airbus = manufacturer("Airbus")
  val Boeing = manufacturer("Boeing")

  // Airplane Models
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

    def inHighSeason(date: Date): Boolean =
      date.in(1 July 2013, 31 August 2013) ||
      date.in(15 December 2013, 31 March 2014)

    val highSeason: PricingScheme = {
      case (Business, date, price) if inHighSeason(date) => price * 1.1
      case (Economy, date, price) if inHighSeason(date) => price * 1.05
    }

    val scheme = highSeason
  })

  val SN = company("SN", "SN Brussels Airlines", new PricingScheme {

    def inSummer(date: Date): Boolean =
      date.in(21 June 2013, 21 September 2013)

    val highSeason: PricingScheme = {
      case (_, date, price) if inSummer(date) => price * 1.1
    }

    val specialActionOnSecondClass: PricingScheme = {
      case (SecondClass, date, price) if date.in(15 July 2013, 15 August 2013) => price - 50.EUR
    }

    val backToSchool: PricingScheme = {
      case (ThirdClass, date, price) if date.in(16 August 2013, 31 August 2013) => price - 10.EUR
    }

    val scheme = highSeason andAlso specialActionOnSecondClass andAlso backToSchool
  })



  // Flights

  val wholeYear = (1 January 2013) -> (31 December 2013)
  val summer = (21 June 2013) -> (21 September 2013)

  val wholeWeek = Monday -> Sunday

  // British Midlands
  FlightTemplate(BM, 1628)(BRU -> CDG, 252.km)(Boeing727) {
    new Schedule()
      .at(9 h 55, every(Monday) during wholeYear) {
      Business -> (25.seats at 250.EUR);
      Economy -> (110.seats at 150.EUR)
    }.at(10 h 9, every(Friday) during summer) {
      Business -> (30.seats at 300.EUR);
      Economy -> (115.seats at 200.EUR)
    }.except(6 January 2013, 25 December 2013)
      .at(15 h 3, 24 December 2013) {
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
