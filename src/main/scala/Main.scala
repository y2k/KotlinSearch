import scala.concurrent.{ExecutionContext, Future, Await}

/*
  String -> String -> String
  'a -> 'a -> Int
*/

case class FuncDescription(args: List[String], result: String)
case class SearchResponse(helpUrl: String, className: String, methodName: String)

object KGoogle {

  def convertToDescription(text: String): Option[FuncDescription] = ???

  def search(msg: FuncDescription): Future[List[SearchResponse]] = ???

}

object MesssageFormater {

  def format(results: List[SearchResponse]): String =
    results match {
      case Nil => "Not found any method signatures"
      case _ => results.map(x => s"• ${x.helpUrl}").fold("Results: \n") { (a, x) => s"$a\n$x" }
    }
}

object BotRepl {

  import com.pengrad.telegrambot.TelegramBot

  def repl(token: String, f: String => Future[Option[String]]): Unit = ???
}

object Main {

  import java.lang.System
  import KGoogle._

  def main(args: Array[String]): Unit = {
    val token = System.getenv("TELEGRAM_TOKEN")
    BotRepl.repl(token, msg => {
      ???
    })
  }
}