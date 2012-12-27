package globair

/**
 * Defines the infrastructure and operations of the DSL
 */
trait FlightDSL extends DelayedInit with DBDefinition {
  self: SQLPopulator =>

  // A JDBC String that identifies the database, e.g.
  // "jdbc:sqlite:test.db", must be provided by implementers of this
  // trait.
  def dbName: String

  import collection.mutable.ListBuffer

  private val initCode = new ListBuffer[() => Unit]
  override def delayedInit(body: => Unit) {
    initCode += (() => body)
  }

  def run() {
    for (proc <- initCode) proc()
    populate(dbName)(
      countries,
      cities,
      airports,
      airlineCompanies,
      manufacturers,
      airplaneModels,
      connections
    )
  }

  // Countries
  private var countries: Vector[Country] = Vector()
  def country(name: String): Country = {
    val country = Country(name)
    countries :+= country
    country
  }

  // Cities
  private var cities: Vector[City] = Vector()
  implicit def cityIn(cityName: String) = new {
    def in(country: Country) = {
      val city = new City(cityName, country)
      cities :+= city
      city
    }
  }

  // Airports
  private var airports: Vector[Airport] = Vector()
  implicit def airportAt(pair: (String, String)) = new {
    def at(city: City): Airport = {
      val airport = new Airport(new AirportCode(pair._1), pair._2, city)
      airports :+= airport
      airport
    }
  }

  // Manufacturers
  private var manufacturers: Vector[Manufacturer] = Vector()
  def manufacturer(name: String): Manufacturer = {
    val manufacturer = Manufacturer(name)
    manufacturers :+= manufacturer
    manufacturer
  }

  // Airline Companies
  private var airlineCompanies: Vector[AirlineCompany] = Vector()
  def company[SeatType <: SeatKind](code: String, name: String,
              pricingScheme: PricingScheme[AirlineCompany, SeatType]): AirlineCompany = {
    val company = new AirlineCompany(AirlineCode(code), name)
    airlineCompanies :+= company
    company
  }

  // Airplane Models
  private var airplaneModels: Vector[AirplaneModel] = Vector()
  implicit def airplaneModelOf(airplaneModelName: String) = new {
    def of(company: Manufacturer) = new {
      def carries(passengers: Int) = new {
        def flies(speed: Int): AirplaneModel = {
          val model = new AirplaneModel(airplaneModelName, passengers, speed, company)
          airplaneModels :+= model
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
  private var connections: Vector[Connection] = Vector()

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


  private var flightTemplates: Vector[FlightTemplate] = Vector()
  def FlightTemplate[SeatType <: SeatKind] (company: AirlineCompany, flightCode: Int)
                                                       (fromTo: (Airport, Airport), distance: Double)
                                                       (airplaneModel: AirplaneModel)
                                                       (schedule: Schedule[SeatType]): FlightTemplate = {
    // TODO make the flights
    // TODO check if total number of seats <= airplaneModel.maxNbOfSeats
    val (from, to) = fromTo
    val conn = new Connection(from, to, distance)
    connections :+= conn
    val ft = new FlightTemplate(new FlightCodeNumber(flightCode.toString), company, conn)
    flightTemplates :+= ft
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
