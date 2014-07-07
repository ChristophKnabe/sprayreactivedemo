//Derived from https://github.com/spray/spray/blob/master/examples/spray-can/simple-http-client/src/main/scala/spray/examples/Main.scala
//2014-06-24 Christoph Knabe

package crawl

import scala.compat.Platform
import scala.concurrent.{Awaitable, Await, Future}
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.event.Logging
import spray.http.{HttpData}

/**Crawls the browsableURIs sequentially and logs statistics about how long it takes for each request and altogether.*/
object SequentialCrawlMain extends App with RequestLevelApiDemo {

  // we always need an ActorSystem to host our application in
  implicit val system = ActorSystem("webcrawl")
  val log = Logging(system, getClass)
  val statisticsActor = StatisticsActor(system, getClass.getSimpleName)

  val uris = browsableURIs.take(9999)

  //Getting the body data from all URIs sequentially.
  //The servers are accessed one after another, as the Futures are constructed only in the body of the for-comprehension
  //and are awaited for completion before going into next iteration.

  for (uri <- uris){
    val bodyFuture: Future[HttpData] = requestBodyData(uri, statisticsActor)
    Await.ready(bodyFuture, 15.seconds)
    bodyFuture.value match {
      case Some(x) => log.info(s"Result: $x")
      case None => throw new java.lang.AssertionError("bodyFuture not ready after call to Await.ready")
    }
  }
  val elapsedMillis = Platform.currentTime - system.startTime
  log.info(s"Getting ${uris.length} URIs lasted $elapsedMillis ms.")
  statisticsActor ! StatisticsActor.Finish

}