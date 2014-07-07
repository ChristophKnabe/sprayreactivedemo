package crawl

import akka.actor._
import akka.event.LoggingAdapter

import scala.collection.mutable.ArrayBuffer
import scala.compat.Platform

/** Actor for collecting statistics information about how long lasts each web request.
  * @author Christoph Knabe
  * @since 2014-07-07
 */
class StatisticsActor(task: String) extends Actor with ActorLogging {

  val meta = StatisticsActor
  meta.startMillis = Platform.currentTime
  log.info(s"Start crawling by $task")

  def receive: Receive = {
    case entry: meta.Entry =>
      meta.archive.append(entry)
    case meta.Finish =>
      meta.finish(log, context.system)
    case unexpected =>
      log.error(s"Received unexpected message:\n$unexpected")
  }

}

/**Meta object for the StatisticsActor*/
object StatisticsActor {

  /**Creates a StatisticsActor in the given ActorSystem with the given task description.*/
  def apply(system: ActorSystem, task: String): ActorRef = {
    system.actorOf(Props(classOf[StatisticsActor], task), name = "statistics")
  }

  /**A statistics entry collecting the information, how long lasted the getting of the page at the uri and how many bytes were contained in the response body.*/
  case class Entry(durationMillis: Long, lengthBytes: Long, uri: String){
    def asMessage = s"$durationMillis millis for $lengthBytes Bytes from $uri"
  }

  case object Finish

  private val archive = new ArrayBuffer[Entry]()

  private var startMillis: Long = Long.MinValue

  private def finish(log: LoggingAdapter, system: ActorSystem){
    val endMillis = Platform.currentTime
    val durationMillis = endMillis - startMillis
    val sorted = archive.sortWith(_.durationMillis > _.durationMillis)
    val summedUpMillis = archive.map(_.durationMillis).reduce(_+_)
    val messages = sorted.map(_.asMessage).mkString("\n")
    log.info("\n{}\nSummary: Crawled {} URIs in {} millis (summedUp: {} millis).", messages, sorted.length, durationMillis, summedUpMillis)
    system.shutdown()
  }

}
