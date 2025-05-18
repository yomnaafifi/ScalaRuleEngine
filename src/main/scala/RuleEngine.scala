import scala.io.Source
import scala.util.Using
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.sql.DriverManager
import RuleEngineLogger.logger

object RuleEngine extends App {
  val lines: List[String] = Using(Source.fromFile("src/main/resources/TRX1000.csv")) { source =>
    source.getLines().drop(1).toList
  }.getOrElse {
    logger.severe("Failed to read the file.")
    List.empty[String]
  }

  case class Order (transaction_date: LocalDate, product_name: String,expiry_date: LocalDate,
                    quantity: Int, unit_price: Double,  channel: String, payment_method: String, discount: Option[Double] = None, finalprice: Option[Double] = None)

  def maptoOrder (line:String): Order = {
    val Array(timestamp, productName, expiryDate, quantity, unitPrice, channel, paymentMethod) = line.split(",")

    val transactionDate = LocalDate.parse(timestamp.split("T")(0))
    val expiry = LocalDate.parse(expiryDate)

    Order(transactionDate, productName, expiry, quantity.toInt, unitPrice.toDouble, channel, paymentMethod)
  }


  //Qualifiers
  def is_lessthan30 (order: Order): Boolean = ChronoUnit.DAYS.between(order.transaction_date, order.expiry_date) < 30
  def is_cheeseorwine(order:Order): Boolean = order.product_name.startsWith("Wine") || order.product_name.startsWith("Cheese")
  def is_soldon23(order:Order): Boolean = order.transaction_date.getMonthValue == 3 && order.transaction_date.getDayOfMonth == 23
  def is_5ofthesame(order:Order): Boolean = order.quantity > 5
  def is_thoughApp(order:Order): Boolean = order.channel.toLowerCase() == "app"
  def is_byVisa(order: Order): Boolean = order.payment_method.toLowerCase() == "visa"
  //Calculators
  def calc_lessthan30 (order: Order): Double = {
    val days = ChronoUnit.DAYS.between(order.transaction_date, order.expiry_date)
    if (days >=1) (30-days)/100.0 else 0.0
  }

  //extract only the name to match
  def calc_cheeseorwine(order:Order): Double =  order.product_name match {
    case name if name.startsWith("Cheese") => 0.10
    case name if name.startsWith("Wine")   => 0.05
  }
  def calc_soldon23(order:Order): Double = 0.5
  def calc_5ofthesame(order:Order): Double = order.quantity match {
      case q if q >= 15 => 0.10
      case q if q >= 10 => 0.07
      case q if q >= 6  => 0.05
    }
  def calc_thoughApp(order:Order): Double = (((order.quantity + 4) / 5) * 0.05)
  def calc_byVisa(order: Order): Double = 0.05


  //applying rules to orders
  val rules: List[(Order => Boolean, Order => Double)] = List(
    (is_lessthan30, calc_lessthan30),
    (is_cheeseorwine, calc_cheeseorwine),
    (is_soldon23, calc_soldon23),
    (is_5ofthesame, calc_5ofthesame),
    (is_thoughApp, calc_thoughApp),
    (is_byVisa, calc_byVisa)

  )

  //a function that calculates discount and the final price then add them to the order object
  def getFinalPrice (order: Order, rules:List[(Order => Boolean, Order => Double)]): Order= {
    val discounts = rules.filter(_._1(order)).map(_._2(order)).sorted.reverse.take(2)
    val discount = if (discounts.nonEmpty) discounts.sum / discounts.length else 0.0
    val finalprice = order.unit_price * (1 - discount)

    order.copy(
      discount = Some(discount),
      finalprice = Some(finalprice)
    )
  }

  //database configurations
  object DBConfig {
    val url = "jdbc:postgresql://localhost:6432/orders_db"
    val user = "scala"
    val password = "scala"
  }

  def writeOrderToDB(order: Order): Unit = {
    val sql =
      """
        |INSERT INTO orders (
        |  transaction_date,
        |  product_name,
        |  expiry_date,
        |  quantity,
        |  unit_price,
        |  channel,
        |  payment_method,
        |  discount,
        |  final_price
        |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """.stripMargin

    Using.Manager { use =>
      val connection = use(DriverManager.getConnection(DBConfig.url, DBConfig.user, DBConfig.password))
      val stmt = use(connection.prepareStatement(sql))

      stmt.setDate(1, java.sql.Date.valueOf(order.transaction_date))
      stmt.setString(2, order.product_name)
      stmt.setDate(3, java.sql.Date.valueOf(order.expiry_date))
      stmt.setInt(4, order.quantity)
      stmt.setDouble(5, order.unit_price)
      stmt.setString(6, order.channel)
      stmt.setString(7, order.payment_method)
      stmt.setObject(8, order.discount.orNull)
      stmt.setObject(9, order.finalprice.orNull)

      stmt.executeUpdate()
    }
  }


  lines.foreach { line =>
    try {
      val order = maptoOrder(line)
      logger.info(s"Parsed order: $order")

      val pricedOrder = getFinalPrice(order, rules)
      logger.info(s"Final price calculated: ${pricedOrder.finalprice.getOrElse("N/A")} for ${pricedOrder.product_name}")

      writeOrderToDB(pricedOrder)
      logger.info(s"Order for ${pricedOrder.product_name} successfully written to DB.")

    } catch {
      case e: Exception =>
        logger.severe(s"Failed to process line: $line. Reason: ${e.getMessage}")
    }
  }


}

