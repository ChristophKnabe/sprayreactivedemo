//Derived from https://github.com/spray/spray/blob/master/examples/spray-can/simple-http-client/src/main/scala/spray/examples/Main.scala
//2014-06-24 Christoph Knabe

package crawl

import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.event.Logging

object SequentialCrawlMain extends App
  with RequestLevelApiDemo {

  // we always need an ActorSystem to host our application in
  implicit val system = ActorSystem("webcrawl")
  import system.dispatcher // execution context for future transformations below
  val log = Logging(system, getClass)

  //Getting the server version from 3 servers sequentially.
  //The servers are accessed one after another, as the Futures are constructed only in the for-comprehension.

  val result = for {
    result1 <- requestProductVersion("spray.io")
    result2 <- requestProductVersion("www.wikipedia.org")
    result3 <- requestProductVersion("scala-lang.org")
  } yield Set(result1, result2, result3)

  result onComplete {
    case Success(res) => log.info("Hosts are running {}", res mkString ", ")
    case Failure(error) => log.warning("Error: {}", error)
  }
  result onComplete { _ => system.shutdown() }

}
