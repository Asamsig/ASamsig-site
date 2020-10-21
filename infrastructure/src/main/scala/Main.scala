import typings.pulumiPulumi.pulumiPulumiRequire

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object Main extends App {
  //Require Pulumi SDK
  pulumiPulumiRequire

  // Export properties from this stack. This prints them at the end of `pulumi up` and
  // makes them accessible via pulumi stack output and visible on the pulumi.com.
  @JSExportTopLevel("Exports")
  object Exports extends js.Object {
    val cloudFrontId = Site.distribution.id
    val contentBucketUri = Site.contentBucket.bucket
    val contentBucketWebsiteEndpoint = Site.contentBucket.websiteEndpoint
    val cloudFrontDomain = Site.distribution.domainName
    val targetDomainEndpoint = s"https://${Site.domainName}/"
    val pathRewriterArn = PathRewriter.pathRewriterArn
  }
}
