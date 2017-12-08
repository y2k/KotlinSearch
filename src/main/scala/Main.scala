import scala.concurrent.{ExecutionContext, Future, Await}

/*
  String -> String -> String
  'a -> 'a -> Int

  Iterable<T>.chunked(size: Int): List<List<T>>
*/

case class SearchRequest(args: List[String], result: String)
case class SearchResponse(helpUrl: String, className: String, methodName: String)

object KGoogle {

  import Effects._

  def convertToDescription(text: String): Option[SearchRequest] = ???

  def search(msg: SearchRequest): Effect[Future[List[SearchResponse]]] = ???

}

object MesssageFormater {

  def format(results: List[SearchResponse]): String =
    results match {
      case Nil => "Not found any method signatures"
      case _ => results
        .map(x => s"``` ${x.className}.${x.methodName} ```\n${x.helpUrl}")
        .mkString("\n\n")
    }
}

object Effects {

  class CanThrow private[Effects]

  type Effect[T] = implicit (CanThrow, ExecutionContext) => T

  def effect(f: Unit => Effect[Unit]) =
    f(Unit)(new CanThrow(), ExecutionContext.global)
}

object BotRepl {

  import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}
  import com.pengrad.telegrambot.request.SendMessage
  import com.pengrad.telegrambot.model.request.ParseMode
  import collection.JavaConverters._
  import Effects._

  def repl(token: String, callback: String => Effect[Future[String]]): Effect[Unit] = {
    val bot = new TelegramBot(token)
    bot.setUpdatesListener(updates => { 
      val tasks =
        updates
          .asScala
          .map(f => callback(f.message.text).map(x => (f.message.from.id, x)))
      val taskResult =
        Future.sequence(tasks)

      taskResult
        .foreach(xs => 
          xs.foreach((id, text) => 
              bot.execute(new SendMessage(id, text).parseMode(ParseMode.Markdown))))

      UpdatesListener.CONFIRMED_UPDATES_ALL
    })
  }
}

object Main {

  import KGoogle._
  import MesssageFormater._
  import Effects._

  def main(args: Array[String]) = effect(_ => {
    val token = System.getenv("TELEGRAM_TOKEN")
    BotRepl.repl(
      token,
      msg => {
        val resp = search(convertToDescription(msg).get)
        resp.map(x => format(x))
      })
  })
}