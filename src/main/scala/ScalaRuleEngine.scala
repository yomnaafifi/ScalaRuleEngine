import scala.io.Source
import java.time.LocalDate
import java.time.temporal.ChronoUnit


object ScalaRuleEngine extends App {
  val lines: List[String] = Source.fromFile("src/main/resources/TRX1000.csv").getLines().drop(1).toList
  case class Order (transaction_date: LocalDate, product_name: String,
                    expiry_date: LocalDate, quantity: Int, unit_price: Double,  channel: String, payment_method: String)

  def maptoOrder (line:String): Order = {
    val Array(timestamp, productName, expiryDate, quantity, unitPrice, channel, paymentMethod) = line.split(",")

    val transactionDate = LocalDate.parse(timestamp.split("T")(0))
    val expiry = LocalDate.parse(expiryDate)

    Order(transactionDate, productName, expiry, quantity.toInt, unitPrice.toDouble, channel, paymentMethod)
  }

  lines.map(maptoOrder).foreach(println)

  //Qualifiers
  def is_lessthan30 (order: Order): Boolean = ChronoUnit.DAYS.between(order.transaction_date, order.expiry_date) < 30
  def is_cheeseorwine(order:Order): Boolean = order.product_name.startsWith("Wine") || order.product_name.startsWith("Cheese")
  def is_soldon23(order:Order): Boolean = order.transaction_date.getMonthValue == 3 && order.transaction_date.getDayOfMonth == 23
  def is_5ofthesame(order:Order): Boolean = order.quantity > 5

  //Calculators
  def calc_lessthan30 (order: Order): Double = {
    val days = ChronoUnit.DAYS.between(order.transaction_date, order.expiry_date)
    if (days >=1) (30-days)/100.0 else 0.0
  }

  def calc_cheeseorwine(order:Order): Double = order.product_name match {
      case "Cheese" => 0.10
      case "Wine" => 0.05
    }

  def calc_soldon23(order:Order): Double = 0.5

  def calc_5ofthesame(order:Order): Double = order.quantity match {
      case q if q >= 15 => 0.10
      case q if q >= 10 => 0.07
      case q if q >= 6  => 0.05
    }



}
