import typings.pulumiAws.{mod => aws}
import typings.pulumiPulumi.pulumiPulumiRequire

import scala.scalajs.js.Date
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object Demo extends App {
  //Require Pulumi SDK
  pulumiPulumiRequire
  println("Start: " + new Date())
  //Create an AWS resource (S3 Bucket)
  val bucket = new aws.s3.Bucket("my-bucket");
  //Export the name of the bucket
  val bucketName = bucket.id
  println("End: " + new Date())
  //    new aws.cloudfront.Distribution("", DistributionArgs(
  //      defaultCacheBehavior = DistributionDefaultCacheBehavior().setLambdaFunctionAssociationsVarargs(DistributionDefaultCacheBehaviorLambdaFunctionAssociation())
  //    ))
  // contentBucket is the S3 bucket that the website's contents will be stored in.
  //    const contentBucket = new aws.s3.Bucket("contentBucket",
  //      {
  //        bucket: config.targetDomain,
  //        acl: "public-read",
  //        // Configure S3 to serve bucket contents as a website. This way S3 will automatically convert
  //        // requests for "foo/" to "foo/index.html".
  //        website: {
  //          indexDocument: "index.html",
  //          errorDocument: "404.html",
  //        },
  //      });

}
