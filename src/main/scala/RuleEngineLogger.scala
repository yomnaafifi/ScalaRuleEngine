import java.util.logging.{Logger, FileHandler, Level, LogRecord}
import java.text.SimpleDateFormat
import java.util.Date

object RuleEngineLogger {

  val logger: Logger = Logger.getLogger("RuleEngineLogger")

  // Create file handler
  private val fileHandler = new FileHandler("src/main/scala/logs/rules_engine.log", true) // true = append mode

  // Define custom formatter
  private object CustomFormatter extends java.util.logging.Formatter {
    private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override def format(record: LogRecord): String = {
      val timestamp = dateFormat.format(new Date(record.getMillis))
      val level = f"${record.getLevel.toString}%-10s"
      val message = record.getMessage
      s"$timestamp   $level   $message\n"
    }
  }

  // Attach the handler
  fileHandler.setFormatter(CustomFormatter)
  logger.addHandler(fileHandler)

  //stop logging to console
  logger.setUseParentHandlers(false)

  // Set logging level
  logger.setLevel(Level.INFO)
}
