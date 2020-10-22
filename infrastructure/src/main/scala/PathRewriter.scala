import Common.awsUsEast1Provider
import typings.pulumiAws.documentsMod.{PolicyDocument, PolicyStatement}
import typings.pulumiAws.lambdaFunctionMod.FunctionArgs
import typings.pulumiAws.roleMod.RoleArgs
import typings.pulumiAws.rolePolicyAttachmentMod.RolePolicyAttachmentArgs
import typings.pulumiAws.{pulumiAwsStrings, mod => aws}
import typings.pulumiPulumi.assetMod.FileArchive
import typings.pulumiPulumi.mod.asset.Archive
import typings.pulumiPulumi.outputMod.Input

import scala.scalajs.js

object PathRewriter {

  private val lambdaName = "PathRewriterLambdaEdge"

  val role = new aws.iam.Role(s"$lambdaName-Role",
    RoleArgs(
      assumeRolePolicy = PolicyDocument(
        js.Array[Input[PolicyStatement]](
          PolicyStatement(pulumiAwsStrings.Allow)
            .setActionVarargs("sts:AssumeRole")
            .setPrincipal(aws.iam.Principals.LambdaPrincipal),
          PolicyStatement(pulumiAwsStrings.Allow)
            .setActionVarargs("sts:AssumeRole")
            .setPrincipal(aws.iam.Principals.EdgeLambdaPrincipal)
        ),
        pulumiAwsStrings.`2012-10-17`
      )
    )
  )

  val rolePolicy = new aws.iam.RolePolicyAttachment(s"$lambdaName-RolePolicyAttachment",
    RolePolicyAttachmentArgs(aws.iam.ManagedPolicies.AWSLambdaBasicExecutionRole, role)
  )

  val archive: Input[Archive] = new FileArchive("../../../../../lambdaPathRewriter/target/universal/lambdapathrewriter-0.1.0-SNAPSHOT.zip").asInstanceOf[Archive]
  val lambda = new aws.lambda.Function(s"$lambdaName-Function",
    FunctionArgs(
      handler = "lambdapathrewriter.handler",
      role = role.arn,
      runtime = typings.pulumiAws.runtimesMod.Runtime.nodejs12Dotx.toString()
    )
      .setCode(archive)
      .setPublish(true),
    awsUsEast1Provider
  )

  // Not using qualifiedArn here due to some bugs around sometimes returning $LATEST
  val pathRewriterArn = lambda.arn[String](arn =>
    lambda.version[String](version => arn + ":" + version)
  )

}
