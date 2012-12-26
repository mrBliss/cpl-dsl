package globair

/**
 * Define how the Entities will be converted to SQL.
 * SQLOutput: will convert Entities to a SQL script
 * SQLOutputFormat (and extending traits): define the format of the
 * SQL (which data types etc.), e.g. SQLite has different types than
 * MySQL
 */

/**
 * Generates a SQL script that creates and populates the tables
 */
trait SQLOutput {
  self: SQLOutputFormat =>

  import DBDSL._
  import java.io.File

  def outputFile: File

  def generate[E <: Entity](entities: Seq[E], createTable: Boolean): Unit = {
    // A non-empty list type would be handy
    if (entities.isEmpty) argError("At least one entity is required")

    // TODO



  }

}

trait SQLOutputFormat {
  def id(name: String): String

  def string(name: String, chars: Int = 100): String
}

trait SQLiteFormat extends SQLOutputFormat {

  def id(name: String) =
    "`%s` INTEGER PRIMARY KEY" format(name)

  def string(name: String, chars: Int = 100) =
    "`%s` VARCHAR(%d) NULL DEFAULT NULL" format(name, chars)

}
