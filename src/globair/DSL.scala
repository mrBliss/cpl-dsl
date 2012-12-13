package globair

object DSL {

  type ID = Int

  class IDGenerator(start: ID = 0) {
    private var nextID = 0
    def next(): ID = {
      val id = nextID
      nextID += 1
      id
    }
  }

  type Time = (Int, Int)

  type Date = Int

  type Price = BigDecimal

  def error(msg: String) = throw new IllegalArgumentException(msg)

  object WeekDay extends Enumeration {
    type WeekDay = Value
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }
  import WeekDay._

  object Country extends Enumeration with Entity {
    type Country = Value
    val Belgium, France, Germany, Italy, Netherlands, Sweden, UK, US = Value
  }
  import Country._

  class City(id: ID, name: String, country: Country) extends Entity

  object City {
    private val id = new IDGenerator()

    def in(country: Country) = new City(id.next(), "TODO", country)

    val Brussels = in(Belgium)
    val Paris = in(France)
    val Frankfurt = in(Germany)
    val Rome = in(Italy)
    val Amsterdam = in(Netherlands)
    val Stockholm = in(Sweden)
    val London = in(UK)
    val NewYork = in(US)
  }

  class AirportCode(code: String) extends Entity {
    require(code matches "[A-Z]{3}",
      "An airport code must consist of 3 capital letters")
  }

  class Airport(code: AirportCode, name: String, city: City) extends Entity

  object Airport {
    import City._
    implicit def foo(pair: (String, String)) = new {
      def at(city: City): Airport = new Airport(new AirportCode(pair._1), pair._2, city)
    }

    val BRU = ("BRU", "Brussels Airport") at Brussels
    val CDG = ("CDG", "Charles de Gaulle Airport") at Paris
    val FRA = ("FRA", "Frankfurst am Main Airport") at Frankfurt
    val CIA = ("CIA", "Rome Ciampino Airport") at Rome
    val AMS = ("AMS", "Amsterdam Airport Schiphol") at Amsterdam
    val ARN = ("ARN", "Stockholm Arlanda Airport") at Stockholm
    val LHR = ("LHR", "London Heathrow Airport") at London
    val JFK = ("JFK", "John F. Kennedy International Airport") at NewYork
  }

  class FlightCodeNumber(val codeNumber: String) extends Entity {
    require(codeNumber matches "[0-9]{3,4}",
      "A flight code number must consist of 3 to 4 digits")
  }

  class AirlineCode(val code: String) extends Entity {
    require(code matches "[A-Z]{2,3}",
      "An airline code must consist of 2 to 3 capital letters")
  }

  class FlightCode(ac: AirlineCode, fcn: FlightCodeNumber) extends Entity {
    override val toString = ac.toString + fcn.toString
  }

  class AirlineCompany(val code: AirlineCode, val name: String) extends Entity

  object AirlineCompany {

    def apply(companyCode: String): AirlineCompany = error("Not implemented")

  }
  class Connection(val from: Airport, val to: Airport, val distance: Double) extends Entity

  class SeatClass(id: ID, name: String, company: AirlineCompany) extends Entity

  class Seat(flight: Flight, number: Int, seatClass: SeatClass) extends Entity

  class Ticket(seat: Seat, price: Price) extends Entity

  class Flight(id: ID, template: FlightTemplate, date: Date,
    moment: FlightMoment, airplane: Airplane) extends Entity

  // TODO link with FlightTemplate?
  class FlightMoment(template: FlightTemplate, weekday: WeekDay,
    time: Time) extends Entity

  // TODO id was code
  class Airplane(id: ID, model: AirplaneModel) extends Entity

  class AirplaneModel(id: ID, maxNbOfSeats: Int, maxSpeed: Double,
    manufacturer: Manufacturer) extends Entity

  class Manufacturer(id: ID, name: String) extends Entity

  import Airport._

  implicit def hourSyntax(hours: Int) = new {
    def h(minutes: Int) = (hours, minutes)
  }

  implicit def kmUnit(kms: Int) = new {
    def km = kms
  }

  class FlightTemplate(codeNumber: FlightCodeNumber,
    val company: AirlineCompany, val connection: Connection) extends Entity {

    def this(codeNumberStr: String, companyCodeStr: String, conn: Connection) =
      this(new FlightCodeNumber(codeNumberStr), AirlineCompany(companyCodeStr), conn)

    lazy val flightCode: FlightCode = new FlightCode(company.code, codeNumber)
  }

  object FlightTemplate {
    val FlightCode = """([A-Z]{2,3})([0-9]{3,4})""".r
    def apply(flightCode: String, fromTo: (Airport, Airport), distance: Double)(when: (Int, Int, Seq[WeekDay])): FlightTemplate = {
      flightCode match {
        case FlightCode(companyCode, flightCode) => {
          val (from, to) = fromTo
          new FlightTemplate(flightCode, companyCode, new Connection(from, to, distance))
        }
        case _ => error("Invalid flight code")
      }
    }
  }

  def every(weekDays: WeekDay*): Seq[WeekDay] = weekDays

  def at(time: (Int, Int), weekDays: Seq[WeekDay]) = (time._1, time._2, weekDays)

  // class AirlineCompany(code: AirlineCode, name: String)

  FlightTemplate("BM1628", BRU -> CDG, 757.km) {
    at(9 h 55, every(Mon, Wed, Fri))
    // by(Boeing727, Business -> 24, Economy, 123)
  }

}

// object SNBrussels extends DSL {

// }
