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

    // Order matters! To populate entity A that keeps a link to entity
    // B, entity B has to be populated first, because we need B's id for A.
    populate(dbName)(
      countries,
      cities,
      airports,
      airlineCompanies,
      manufacturers,
      airplaneModels,
      connections,
      seatTypes,
      flightTemplates,
      flights,
      seatPricings
    )
    // TODO it's quite slow (due to printing?)
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

  // Airplane Models
  private var airplaneModels: Vector[AirplaneModel] = Vector()
  implicit def airplaneModelOf(airplaneModelName: String) =  new {
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

  // Airline Companies
  private var airlineCompanies: Vector[AirlineCompany] = Vector()
  private var pricingSchemes: Map[AirlineCompany, PricingScheme] = Map()
  def company(code: String, name: String,
              pricingScheme: => PricingScheme): AirlineCompany = {
    val company = new AirlineCompany(AirlineCode(code), name)
    airlineCompanies :+= company
    pricingSchemes += (company -> (pricingScheme))
    company
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

  import DBDSL.Price
  implicit def currenctySyntax(amount: Int) = new {
    def EUR: Price = BigDecimal(amount)
  }

  implicit def seatsSyntax(nbOfSeats: Int) = new {
    def seats = new {
      def at(price: Price): (Int, Price) = (nbOfSeats, price)
    }
  }

  def every(weekDay: WeekDay): WeekDay = weekDay


  private var flightTemplates: Vector[FlightTemplate] = Vector()
  private var flights: Vector[Flight] = Vector()
  private var seatPricings: Vector[SeatPricing] = Vector()
  def FlightTemplate(company: AirlineCompany, flightCode: Int)
                    (fromTo: (Airport, Airport), distance: Double)
                    (airplaneModel: AirplaneModel)
                    (schedule: Schedule): Unit = {
    // TODO check if total number of seats <= airplaneModel.maxNbOfSeats
    val (from, to) = fromTo

    // Save the connection
    val conn = new Connection(from, to, distance)
    connections :+= conn

    // Save the FlightTemplate
    val flightTemplate = new FlightTemplate(new FlightCodeNumber(flightCode.toString), company, conn)
    flightTemplates :+= flightTemplate

    // Flights
    val newFlights: Seq[Flight] = schedule.schedule map(s => Flight(flightTemplate, s._1, airplaneModel))
    flights ++= newFlights

    // SeatPricings
    val pricingScheme = pricingSchemes(company)
    val newSeatPricings: Seq[SeatPricing] = for {
      (flight, (dateTime, seating)) <- newFlights zip schedule.schedule
      (seatType, (nbSeats, initPrice)) <- seating
      price = pricingScheme(seatType, dateTime.toDate, initPrice)
    } yield SeatPricing(seatType, flight, price, nbSeats)
    seatPricings ++= newSeatPricings
  }

  // SeatTypes
  private var seatTypes: Vector[SeatType] = Vector()
  def seatType(name: String): SeatType = {
    val st = SeatType(name)
    seatTypes :+= st
    st
  }



  import globair.DBDSL.Price

  trait PricingScheme {

    // Type alias
    type PricingScheme = PartialFunction[(SeatType, Date, Price), Price]

    private val defaultScheme: PricingScheme = {
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

    def apply(seatType: SeatType, date: Date, price: Price): Price = (scheme orElse defaultScheme)(seatType, date, price)

    // Abstract, must be provided when defining a PricingScheme
    val scheme: PricingScheme

  }

  class Schedule(val schedule: Seq[(DateTime, Map[SeatType, (Int, Price)])] = Vector()) {

    def at(time: Time, dates: Seq[Date])(seats: (SeatType, (Int, Price))*): Schedule = {
      val seatMap = Map(seats:_*)
      new Schedule(schedule ++ (dates map (date => (date toDateTime time, seatMap))))
    }

    def at(time: Time, date: Date)(seats: (SeatType, (Int, Price))*): Schedule =
      new Schedule(schedule :+ (date toDateTime time, Map(seats:_*)))

    def except(dates: Date*): Schedule =
      new Schedule(schedule filterNot { case (dateTime, _) => dates exists (dateTime sameDay _) })

  }


}
