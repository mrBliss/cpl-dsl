package globair

object Example extends DB with FlightDSL {
  // Countries
  val Belgium     = country("Belgium")
  val France      = country("France")
  val Germany     = country("Germany")
  val Italy       = country("Italy")
  val Netherlands = country("Netherlands")
  val Sweden      = country("Sweden")
  val UK          = country("United Kingdom")
  val US          = country("United States")

  // Cities
  val Brussels  = "Brussels"  in Belgium
  val Paris     = "Paris"     in France
  val Frankfurt = "Frankfurt" in Germany
  val Rome      = "Rome"      in Italy
  val Amsterdam = "Amsterdam" in Netherlands
  val Stockholm = "Stockholm" in Sweden
  val London    = "London"    in UK
  val NewYork   = "New York"  in US

  // Airports
  val BRU = ("BRU", "Brussels Airport")                      at Brussels
  val CDG = ("CDG", "Charles de Gaulle Airport")             at Paris
  val FRA = ("FRA", "Frankfurst am Main Airport")            at Frankfurt
  val CIA = ("CIA", "Rome Ciampino Airport")                 at Rome
  val AMS = ("AMS", "Amsterdam Airport Schiphol")            at Amsterdam
  val ARN = ("ARN", "Stockholm Arlanda Airport")             at Stockholm
  val LHR = ("LHR", "London Heathrow Airport")               at London
  val JFK = ("JFK", "John F. Kennedy International Airport") at NewYork

  import DatabaseDSL._

  // Flights
  FlightTemplate("BM1628", BRU -> CDG, 757.km) {
    at(9 h 55, every(Monday, Wednesday, Friday))
    // by(Boeing727, Business -> 24, Economy, 123)
  }

}
