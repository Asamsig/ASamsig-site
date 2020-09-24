import Common.awsUsEast1Provider
import net.exoego.facade.aws_lambda.{CloudFrontRequestEvent, CloudFrontRequestResult}
import typings.pulumiAws.documentsMod.{PolicyDocument, PolicyStatement}
import typings.pulumiAws.lambdaMixinsMod.CallbackFunctionArgs
import typings.pulumiAws.pulumiAwsStrings
import typings.pulumiAws.roleMod.RoleArgs
import typings.pulumiAws.rolePolicyAttachmentMod.RolePolicyAttachmentArgs
import typings.pulumiAws.{mod => aws}
import typings.pulumiPulumi.outputMod.Input

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.RegExp
import scala.scalajs.js.annotation.JSExport
import typings.pulumiPulumi.{mod => pulumi}

object PathRewriter {

  val lambdaName = "PathRewriterLambdaEdge"

  val policyStatement: js.Array[Input[PolicyStatement]] = js.Array(
    PolicyStatement(pulumiAwsStrings.Allow)
      .setActionVarargs("sts:AssumeRole")
      .setPrincipal(aws.iam.Principals.LambdaPrincipal),
    PolicyStatement(pulumiAwsStrings.Allow)
      .setActionVarargs("sts:AssumeRole")
      .setPrincipal(aws.iam.Principals.EdgeLambdaPrincipal)
  )

  val role = new aws.iam.Role(s"$lambdaName-Role",
    RoleArgs(
      assumeRolePolicy = PolicyDocument(
        policyStatement,
        pulumiAwsStrings.`2012-10-17`
      )
    )
  )

  val rolePolicy = new aws.iam.RolePolicyAttachment(s"$lambdaName-RolePolicyAttachment",
    RolePolicyAttachmentArgs(aws.iam.ManagedPolicies.AWSLambdaBasicExecutionRole, role)
  )

  import js.JSConverters._

  implicit val ec = ExecutionContext.global

  val lambda = new aws.lambda.CallbackFunction(s"$lambdaName-Function",
    CallbackFunctionArgs[CloudFrontRequestEvent, CloudFrontRequestResult]()
      .setRole(role)
      .setRuntime(typings.pulumiAws.runtimesMod.Runtime.nodejs12Dotx)
      .setCallback { case (event, context, _) =>
        val pointsToFile: String => Boolean = uri => RegExp("""/\/[^/]+\.[^/]+$/""").test(uri)
        val hasTrailingSlash: String => Boolean = uri => uri.endsWith("/")
        val request = event.Records(0).cf.request
        val oldUri = request.uri
        if (pointsToFile(oldUri)) {
          Future(request).toJSPromise
        } else {
          if (hasTrailingSlash(oldUri)) {
            request.uri = oldUri + "index.html"
          } else {
            request.uri = oldUri + "/index.html"
          }
          Future(request).toJSPromise
        }
      },
    awsUsEast1Provider
      .set("publish", true)

  )

  // Not using qualifiedArn here due to some bugs around sometimes returning $LATEST
  //  export default pulumi.interpolates "${lambda.arn}:${lambda.version}";
//  @JSExport
  val pathRewriterArn = lambda.arn//pulumi.concat(lambda.arn.asInstanceOf[Input[String]], ":".asInstanceOf[Input[String]], lambda.version.asInstanceOf[Input[String]])

}
