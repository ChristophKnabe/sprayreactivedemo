//Example from https://github.com/spray/spray/tree/release/1.3/examples/spray-can/simple-http-client/src/main/scala/spray/examples
//2014-06-20 Christoph Knabe

package crawl

import spray.http.{HttpData, ProductVersion}

import scala.compat.Platform
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import akka.actor.ActorSystem
import akka.event.Logging
import scala.util.control.NonFatal

/**Crawls the browsableURIs asynchronously by Futures and logs statistics about how long it takes for each request and altogether.*/
object FutureCrawlMain extends App with RequestLevelApiDemo {

  // we always need an ActorSystem to host our application in
  implicit val system = ActorSystem("webcrawl")
  import system.dispatcher // execution context for future transformations below
  val log = Logging(system, getClass)
  val statisticsActor = StatisticsActor(system, getClass.getSimpleName)

  val uris = browsableURIs.take(9999)

  sealed trait Result
  case class Returns(uri: String, httpData: HttpData) extends Result
  case class DidntAnswer(uri: String, problem: String) extends Result

  def requestWithErrorHandling(uri: String): Future[Result] =
    requestBodyData(uri, statisticsActor).map {
      httpData => Returns(uri, httpData)
    }.recover { case NonFatal(e) => DidntAnswer(uri, e.getMessage) }


  //The futures are constructed immediately one after another and are then running.
  val futures = uris.map(requestWithErrorHandling)

  //Collect the results of all requests:
  val result = Future.sequence(futures)

  def shutdown(){
    val elapsedMillis = Platform.currentTime - system.startTime
    log.info(s"Getting ${uris.length} URIs lasted $elapsedMillis ms.")
    statisticsActor ! StatisticsActor.Finish
  }

  val reportSeq: (Try[Seq[Result]]) => Unit = {
    case Success(res) =>
      log.info("Result:")
      res.sortBy {
        case _: Returns => 0
        case _ => 2
      }.foreach(x => log.info(x.toString))
      shutdown()
    case Failure(error) =>
      log.warning("Error: {}", error)
      shutdown()
  }

  result onComplete reportSeq

}
