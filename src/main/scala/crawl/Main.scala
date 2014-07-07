//2014-06-20 Christoph Knabe

package crawl

import spray.http.ProductVersion

import scala.collection.immutable.Nil
import scala.compat.Platform
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import akka.actor.ActorSystem
import akka.event.Logging
import scala.util.control.NonFatal

/**Main class of a web crawler demo for usage on the command line with arguments.*/
object Main extends App
  with RequestLevelApiDemo {

  val specVersion = sys.props("java.specification.version")
  assert(specVersion >= "1.7", "Java 1.7 or above required, but " + specVersion + " is present.")
  val restArgs: Array[String] = args.drop(1)
  val argsList = args.toList
  argsList match {
    case Nil => exitWithUsageError("Missing variant: Must be one of -s -f -a.")
    case List("-s") => SequentialCrawlMain.main(restArgs)
    case List("-f") => FutureCrawlMain.main(restArgs)
    case List("-a") => ActorPerRequestCrawlMain.main(restArgs)
    case x => exitWithUsageError("Wrong variant or extraneous arguments: " + args.mkString(" "))
  }

  def exitWithUsageError(messagePrefix: String){
    Console.err.println(
      messagePrefix + '\n' +
      """Usage:
        |java -jar sprayreactivedemo.jar variant
        |where variant can be
        |  -s   Sequentially crawl the web
        |  -f   use Futures to parallelize the web accesses
        |  -a   use Actors to parallelize the web access
      """.stripMargin
    )
    sys.exit(-1)
  }

}
