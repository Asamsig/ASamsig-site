import net.exoego.facade.aws_lambda._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.{Promise, RegExp}

object PathRewriterLambda {

  def rewritePath(event: CloudFrontRequestEvent)(implicit executionContext: ExecutionContext): Future[CloudFrontRequestResult] = {
    val pointsToFile: String => Boolean = uri => RegExp("""/\/[^/]+\.[^/]+$/""").test(uri)
    val hasTrailingSlash: String => Boolean = uri => uri.endsWith("/")
    val request = event.Records(0).cf.request
    val oldUri = request.uri
    if (pointsToFile(oldUri)) {
      Future(request)
    } else {
      if (hasTrailingSlash(oldUri)) {
        request.uri = oldUri + "index.html"
      } else {
        request.uri = oldUri + "/index.html"
      }
      Future(request)
    }
  }

  @JSExportTopLevel(name = "handler")
  def apply(event: CloudFrontRequestEvent, context: Context): Promise[CloudFrontRequestResult] = {
    import js.JSConverters._
    implicit val ec = ExecutionContext.global
    rewritePath(event).toJSPromise
  }
}
