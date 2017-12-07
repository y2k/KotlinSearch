import scala.concurrent.{ExecutionContext, Future, Await}

/*
  String -> String -> String
  'a -> 'a -> Int

  Iterable<T>.chunked(size: Int): List<List<T>>
*/

case class SearchRequest(args: List[String], result: String)
case class SearchResponse(helpUrl: String, className: String, methodName: String)

object KGoogle {

  def convertToDescription(text: String): Option[SearchRequest] = ???

  def search(msg: SearchRequest): Future[List[SearchResponse]] = ???

}

object MesssageFormater {

  def format(results: List[SearchResponse]): String =
    results match {
      case Nil => "Not found any method signatures"
      case _ => results
        .map(x => s"``` ${x.className}.${x.methodName}```\n${x.helpUrl}")
        .mkString("\n\n")
    }
}

object BotRepl {

  import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}
  import com.pengrad.telegrambot.request.SendMessage
  import com.pengrad.telegrambot.model.request.ParseMode
  import collection.JavaConverters._

  def repl(token: String, callback: String => Future[Option[String]])(implicit ctx: ExecutionContext): Unit = {
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
          xs.collect { case (id, Some(text)) => (id, text) }
            .foreach((id, text) => 
              bot.execute(new SendMessage(id, text).parseMode(ParseMode.Markdown))))

      UpdatesListener.CONFIRMED_UPDATES_ALL
    })
  }
}

object Main {

  import java.lang.System
  import KGoogle._
  import MesssageFormater._

  def main(args: Array[String]): Unit = {
    implicit val ctx = ExecutionContext.global
    val token = System.getenv("TELEGRAM_TOKEN")
    BotRepl.repl(token, msg => {
        try {
            val r = convertToDescription(msg).get
            val resp = search(r)
            val resMsg = resp.map(x => Some(format(x)))
            resMsg
        } catch {
          case _ => Future(None)
        }
    })
  }
}