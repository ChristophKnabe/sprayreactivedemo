//Example from https://github.com/spray/spray/tree/release/1.3/examples/spray-can/simple-http-client/src/main/scala/spray/examples
//2014-06-20 Christoph Knabe

package crawl

import akka.io.IO
import spray.can.Http
import spray.http.HttpMethods._
import spray.http.{Uri, HttpRequest, ProductVersion}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import akka.actor.{ActorLogging, Actor, Props, ActorSystem}
import akka.event.Logging

object ActorCrawlMain extends App
  with RequestLevelApiDemo {

  // we always need an ActorSystem to host our application in
  implicit val system = ActorSystem("webcrawl")
  import system.dispatcher // execution context for future transformations below
  val log = Logging(system, getClass)

  // the spray-can client-side API has three levels (from lowest to highest):
  // 1. the connection-level API
  // 2. the host-level API
  // 3. the request-level API
  //
  // this example demonstrates the request-level API by retrieving the server-version
  // of http://spray.io in three different ways

  val host = "spray.io"
  val requestActor = system.actorOf(Props(new RequestActor(host)), name = "requestActor")
  val executor = system.dispatcher
  system.scheduler.scheduleOnce(10.seconds){system.shutdown()}

}

class RequestActor(host: String) extends Actor with ActorLogging {

  import context.system
  
  IO(Http).tell(HttpRequest(GET, Uri(s"http://$host/")), self)

  def receive: Receive = {
    case x => log.debug(x.toString)
  }

}


