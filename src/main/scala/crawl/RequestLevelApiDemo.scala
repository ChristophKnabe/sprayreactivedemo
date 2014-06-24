//Example from https://github.com/spray/spray/tree/release/1.3/examples/spray-can/simple-http-client/src/main/scala/spray/examples
//2014-06-20 Christoph Knabe

package crawl

import scala.compat.Platform
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.http._
import spray.can.Http
import HttpMethods._

/**Taken nearly unchanged from https://github.com/spray/spray/blob/master/examples/spray-can/simple-http-client/src/main/scala/spray/examples/RequestLevelApiDemo.scala. */
trait RequestLevelApiDemo {

  /**Timeout value for IO.ask method.*/
  private implicit val timeout: Timeout = 10.seconds


  // The request-level API is the highest-level way to access the spray-can client-side infrastructure.
  // All you have to do is to send an HttpRequest instance to `IO(Http)` and wait for the response.
  // The spray-can HTTP infrastructure looks at the URI (or the Host header if the URI is not absolute)
  // to figure out which host to send the request to. It then sets up a HostConnector for that host
  // (if it doesn't exist yet) and forwards it the request.
  /**A slightly modified version of the method demoRequestLevelApi from the official Spray demo.*/
  def requestProductVersion(uri: String)(implicit system: ActorSystem): Future[Option[ProductVersion]] = {
    val uriCompleted = if(uri.indexOf('/') < 0) uri+'/' else uri
    import system.dispatcher  // execution context for future transformation below
    val startTime = Platform.currentTime
    for {
      response <- IO(Http).ask(HttpRequest(GET, Uri(s"http://$uri"))).mapTo[HttpResponse]
      _ <- IO(Http) ? Http.CloseAll
    } yield {
      val startAtMillis = startTime - system.startTime
      val durationInMillis = Platform.currentTime - startTime
      system.log.info(s"Get URI $uri responded '${response.status}' with ${response.entity.data.length} bytes. Started at $startAtMillis ms, lasted $durationInMillis ms.")
      response.header[HttpHeaders.Server].map(_.products.head)
    }
  }

}
