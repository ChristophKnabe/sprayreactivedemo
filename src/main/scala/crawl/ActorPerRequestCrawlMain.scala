//Example from https://github.com/spray/spray/tree/release/1.3/examples/spray-can/simple-http-client/src/main/scala/spray/examples
//2014-06-20 Christoph Knabe

package crawl

import akka.io.{Tcp, IO}
import spray.can.Http
import spray.http.HttpMethods._
import spray.http._

import scala.compat.Platform
import scala.concurrent.duration._
import akka.actor._
import akka.event.Logging

/**Crawls the browsableURIs asynchronously by many Actors and logs statistics about how long it takes for each request and altogether.*/
object ActorPerRequestCrawlMain extends App {

  // we always need an ActorSystem to host our application in
  implicit val system = ActorSystem("webcrawl")
  val log = Logging(system, getClass)
  val statisticsActor = StatisticsActor(system, getClass.getSimpleName)

  val uris = browsableURIs.take(9999)
  val managerActor = system.actorOf(Props(classOf[ManagerActor], uris, statisticsActor), name = "manager")

}

class ManagerActor(uris: Seq[String], statisticsActor: ActorRef) extends Actor with ActorLogging {
  var openRequestsCount = uris.length
  for(uri <- uris){
    val name = uri.replaceAll("/", "_slash_")
    val requestActor = context.actorOf(Props(classOf[RequestActor], uri, statisticsActor), name)
  }

  def receive: Receive = {
    case completion: RequestCompletion =>
      openRequestsCount -= 1
      log.info(s"Manager received: $completion. Open requests: $openRequestsCount")
      if(openRequestsCount <= 0){
        val elapsedMillis = Platform.currentTime - context.system.startTime
        log.info(s"Getting ${uris.length} URIs lasted $elapsedMillis ms.")
        statisticsActor ! StatisticsActor.Finish
      }
    case unexpected =>
      log.error(s"Received unexpected message:\n$unexpected")
  }
}

/**Messages to the ManagerActor from a RequestActor signaling completion of its work.*/
sealed abstract class RequestCompletion(uri: String)
case class RequestSucceeded(uri: String) extends RequestCompletion(uri)
case class RequestFailed(uri: String, hint: Any) extends RequestCompletion(uri)

/**Sends a request to the given URI by HTTP and logs the response. Made by the Actor-per-Request pattern.*/
class RequestActor(httpURI: String, statisticsActor: ActorRef) extends Actor with ActorLogging {
  val uriCompleted = "http://" + {if(httpURI.indexOf('/') < 0) httpURI+'/' else httpURI}

  import context.system
  val startTime = Platform.currentTime
  context.setReceiveTimeout(10.seconds)
  
  IO(Http).tell(HttpRequest(GET, Uri(uriCompleted), headers=List(HttpHeaders.Accept(MediaRanges.`text/*`))), self)

  def receive: Receive = {
    case response: HttpResponse =>
      val startAtMillis = startTime - system.startTime
      val durationInMillis = Platform.currentTime - startTime
      val lengthBytes: Long = response.entity.data.length
      log.info(s"Get URI $httpURI responded '${response.status}' with ${lengthBytes} bytes. Started at $startAtMillis ms, lasted $durationInMillis ms."
        //+ s"\nHeaders: ${response.headers}" //+ s"\n${response.entity.data.asString}"
      )
      context.parent ! RequestSucceeded(httpURI)
      statisticsActor ! StatisticsActor.Entry(durationInMillis, lengthBytes, httpURI)
      self ! PoisonPill
    case unexpected =>
      context.parent ! RequestFailed(httpURI, unexpected)
      self ! PoisonPill
  }

}


