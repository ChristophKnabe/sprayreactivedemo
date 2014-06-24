//Example from https://github.com/spray/spray/tree/release/1.3/examples/spray-can/simple-http-client/src/main/scala/spray/examples
//2014-06-20 Christoph Knabe

package crawl

import spray.http.ProductVersion

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import akka.actor.ActorSystem
import akka.event.Logging
import scala.util.control.NonFatal

object FutureCrawlMain extends App
  with RequestLevelApiDemo {

  // we always need an ActorSystem to host our application in
  implicit val system = ActorSystem("webcrawl")
  import system.dispatcher // execution context for future transformations below
  val log = Logging(system, getClass)

  val uris = Seq(
    "spray.io", "www.wikipedia.org"
  //TODO Works well with only 2 URIs, sometimes also with 3 URIs, but does not scale to e.g. 5 URIs!
   ,"stackoverflow.com/questions/tagged/scala"
    , "scala-lang.org"
   , "doc.akka.io", "public.beuth-hochschule.de/~knabe/", "fb6.beuth-hochschule.de"
   , "esperanto.de", "tatoeba.org"
  )

  sealed trait Result
  case class Runs(uri: String, productVersion: ProductVersion) extends Result
  case class NoServerHeader(uri: String) extends Result
  case class DidntAnswer(uri: String, problem: String) extends Result

  def requestWithErrorHandling(uri: String): Future[Result] =
    requestProductVersion(uri).map {
      case Some(version) => Runs(uri, version)
      case None => NoServerHeader(uri)
    }.recover { case NonFatal(e) => DidntAnswer(uri, e.getMessage) }

  //The futures are constructed immediately one after another and are then running.
  val futures = uris.map(requestWithErrorHandling)

  //Collect the results of all requests:
  val result = Future.sequence(futures)

  def shutdown(){
    log.info("Shutting ActorSystem down...")
    system.shutdown()
  }

  val reportSeq: (Try[Seq[Result]]) => Unit = {
    case Success(res) =>
      log.info("Result:")
      res.sortBy {
        case _: Runs => 0
        case _:NoServerHeader => 1
        case _ => 2
      }.foreach(x => log.info(x.toString))

      shutdown()
    case Failure(error) => log.warning("Error: {}", error); shutdown()
  }

  result onComplete reportSeq

}
