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

  // We just need one entity of the right type, it doesn't matter its
  // instance variables are.
//   def generate[E <: Entity](entity: E): Unit = {

//     "DROP TABLE IF EXISTS `%s`;\n\nCREATE TABLE `%s` (\n" +
//     entity.row.map((k, v) =>
// //   `id` INTEGER PRIMARY KEY,
// //   `name` VARCHAR(100) NULL DEFAULT NULL,
// //   `id_Country` INT NOT NULL DEFAULT NULL,
// //   FOREIGN KEY (id_Country) REFERENCES `Country` (`id`)
// // );

//   }

  def insert[E <: Entity](entities: Seq[E]): Unit = {
    // TODO
  }

}

trait SQLOutputFormat {
  import DBDSL.Entity

  def id(name: String): String

  def foreignKey(foreignEntity: Entity)(refColumn: String): String

  def string(chars: Int = 100, default: String = "NULL")(name: String) : String

  def int(name: String): String

  def double(name: String): String

  def money(name: String): String

  def unique(fields: String*): String

  def dateTime(name: String): String

}

trait SQLiteFormat extends SQLOutputFormat {
  import DBDSL.Entity

  def id(name: String) =
    "`%s` INTEGER PRIMARY KEY" format(name)

  // TODO key of type than auto_increment

  def foreignKey(foreignEntity: Entity)(refColumn: String) = {
    val keyName = "%s_%s" format(refColumn, foreignEntity.tableName)
    foreignEntity.key.field.mkStatement(this)(refColumn) +
    "FOREIGN KEY (%s) REFERENCES `%s` (`%s`)" format(keyName, foreignEntity.tableName, refColumn)
  }

  def string(chars: Int = 100, default: String = "NULL")(name: String) =
    "`%s` VARCHAR(%d) NOT NULL DEFAULT %s" format(name, chars, default)

  def int(name: String) = "`%s` INT NOT NULL DEFAULT NULL" format name

  def double(name: String) = "`%s` DOUBLE NOT NULL DEFAULT NULL" format name

  def money(name: String) = "`%s` DECIMAL(19,4) NOT NULL DEFAULT NULL" format name

  def unique(fields: String*) =
    "UNIQUE (%s)" format(fields.map("`_`" format _).mkString(", "))

  def dateTime(name: String) = "`%s` DATETIME NOT NULL DEFAULT 'NULL'" format name

}
  //   `id_City` INT NOT NULL DEFAULT NULL,
  //   `code_toAirport` VARCHAR(3) NOT NULL DEFAULT '???',
