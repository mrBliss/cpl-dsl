package globair

object Example extends DB with FlightDSL {
  import DatabaseDSL._

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
  val Airbus = Manufacturer("Airbus")
  val Boeing = Manufacturer("Boeing")
  val Cessna = Manufacturer("Cessna")

  // Airline Companies (the code is the official IATA code)
  val BM = company("BM", "British Midlands Airways")
  val SN = company("SN", "SN Brussels Airlines")
  val FR = company("FR", "Ryanair")
  val QF = company("QF", "Qantas Airways")
  val LH = company("LH", "Deutsche Lufthansa")

  // Airplane Models
  val AirbusA320 = "Airbus A320" of Airbus carries 150.p flies 828.kmh
  val AirbusA380 = "Airbus A380" of Airbus carries 644.p flies 945.kmh
  val Boeing727 = "Boeing 727" of Boeing carries 145.p flies 963.kmh
  val Boeing737_800 = "Boeing 737-800" of Boeing carries 160.p flies 828.kmh

  // Flights
  FlightTemplate(BM, 1628)(BRU -> CDG, 757.km) {
    at(9 h 55, every(Monday, Wednesday, Friday))
    // by(Boeing727, Business -> 24, Economy, 123)
  }


}
