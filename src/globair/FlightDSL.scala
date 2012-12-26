package globair

/**
 * Defines the infrastructure and operations of the DSL
 */
trait FlightDSL extends DelayedInit with DBDefinition with SQLOutput {
  self: SQLOutputFormat =>

  import collection.mutable.ListBuffer

  private val initCode = new ListBuffer[() => Unit]
  override def delayedInit(body: => Unit) {
    initCode += (() => body)
  }

  def run() {
    for (proc <- initCode) proc()

    // createTable(Country)
    // println(countries)
    // println(cities)
    // println(airports)
    // println(airlineCompanies)
    // println(airplaneModels)
    // println(connections)
    // println(flightTemplates)
    // // TODO pricing of seats
    // println("DONE")
  }

  // Countries
  private var countries: List[Country] = Nil
  def country(name: String): Country = {
    val country = Country(name)
    countries ::= country
    country
  }

  // Cities
  private var cities: List[City] = Nil
  implicit def cityIn(cityName: String) = new {
    def in(country: Country) = {
      val city = new City(cityName, country)
      cities ::= city
      city
    }
  }

  // Airports
  private var airports: List[Airport] = Nil
  implicit def airportAt(pair: (String, String)) = new {
    def at(city: City): Airport = {
      val airport = new Airport(new AirportCode(pair._1), pair._2, city)
      airports ::= airport
      airport
    }
  }

  // Airline Companies
  private var airlineCompanies: List[AirlineCompany] = Nil
  def company[SeatType <: SeatKind](code: String, name: String,
              pricingScheme: PricingScheme[AirlineCompany, SeatType]): AirlineCompany = {
    val company = new AirlineCompany(AirlineCode(code), name)
    airlineCompanies ::= company
    company
  }

  // Airplane Models
  private var airplaneModels: List[AirplaneModel] = Nil
  implicit def airplaneModelOf(airplaneModelName: String) = new {
    def of(company: Manufacturer) = new {
      def carries(passengers: Int) = new {
        def flies(speed: Int): AirplaneModel = {
          val model = new AirplaneModel(airplaneModelName, passengers, speed, company)
          airplaneModels ::= model
          model
        }
      }
    }
  }

  implicit def pUnit(ps: Int) = new {
    def p: Int = ps
  }

  implicit def kmhUnit(kmhs: Int) = new {
    def kmh: Int = kmhs
  }

  // Connections
  private var connections: List[Connection] = Nil

  import org.joda.time.Interval

  // Flights

  implicit def dayRange(weekDay: WeekDay) = new {
    def during(interval: Interval): Seq[Date] = weekDay in interval
  }

  implicit def hourSyntax(hours: Int) = new {
    def h(minutes: Int) = Time(hours, minutes)
  }

  implicit def kmUnit(kms: Int) = new {
    def km: Double = kms
  }

  implicit def seatsSyntax(seats: Int) = new {
    def seats: Int = seats
  }

  def every(weekDay: WeekDay): WeekDay = weekDay


  private var flightTemplates: List[FlightTemplate] = Nil
  def FlightTemplate[SeatType <: SeatKind] (company: AirlineCompany, flightCode: Int)
                                                       (fromTo: (Airport, Airport), distance: Double)
                                                       (airplaneModel: AirplaneModel)
                                                       (schedule: Schedule[SeatType]): FlightTemplate = {
    // TODO make the flights
    // TODO check if total number of seats <= airplaneModel.maxNbOfSeats
    val (from, to) = fromTo
    val conn = new Connection(from, to, distance)
    connections ::= conn
    val ft = new FlightTemplate(new FlightCodeNumber(flightCode), company, conn)
    flightTemplates ::= ft
    ft
  }

  abstract class SeatKind

  import globair.DBDSL.Price

  trait PricingScheme[Company, SeatType <: SeatKind] {

    // Type alias
    type PricingScheme = PartialFunction[(SeatType, Date, Price), Price]

    val defaultScheme: PricingScheme = {
      case (_, _, price) => price
    }

    implicit def andAlsoFunc(schemeA: PricingScheme) = new {
      def andAlso(schemeB: PricingScheme): PricingScheme =
        new PartialFunction[(SeatType, Date, Price), Price] {
          def isDefinedAt(x: (SeatType, Date, Price)): Boolean =
            schemeA.isDefinedAt(x) || schemeB.isDefinedAt(x)
          def apply(x: (SeatType, Date, Price)): Price =
              if (schemeA.isDefinedAt(x)) {
                if (schemeB.isDefinedAt(x))
                  schemeB.apply((x._1, x._2, schemeA.apply(x)))
                else
                  schemeA.apply(x)
              } else {
                schemeB.apply(x)
              }
        }
    }

    // Abstract, must be provided when defining a PricingScheme
    val scheme: PricingScheme
  }

  class Schedule[SeatType <: SeatKind](schedule: Seq[(Time, Date, Map[SeatType, Int])] = Vector()) {
    def at(time: Time, dates: Seq[Date])(seats: (SeatType, Int)*): Schedule[SeatType] = {
      val seatMap = Map(seats:_*)
      new Schedule(schedule ++ (dates map (date => (time, date, seatMap))))
    }

    def at(time: Time, date: Date)(seats: (SeatType, Int)*): Schedule[SeatType] =
      new Schedule(schedule :+ (time, date, Map(seats:_*)))

    def except(dates: Date*): Schedule[SeatType] =
      new Schedule(schedule filterNot (x => dates contains x._2))
  }


}