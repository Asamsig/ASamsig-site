import Common.awsUsEast1Provider
import typings.pulumiAws.bucketMod.BucketArgs
import typings.pulumiAws.bucketPolicyMod.BucketPolicyArgs
import typings.pulumiAws.bucketPublicAccessBlockMod.BucketPublicAccessBlockArgs
import typings.pulumiAws.cannedAclMod.CannedAcl
import typings.pulumiAws.certificateMod.CertificateArgs
import typings.pulumiAws.certificateValidationMod.CertificateValidationArgs
import typings.pulumiAws.distributionMod.DistributionArgs
import typings.pulumiAws.documentsMod.{AWSPrincipal, PolicyDocument, PolicyStatement}
import typings.pulumiAws.getZoneMod.GetZoneArgs
import typings.pulumiAws.inputMod.cloudfront._
import typings.pulumiAws.inputMod.route53.RecordAlias
import typings.pulumiAws.inputMod.s3.{BucketServerSideEncryptionConfiguration, BucketServerSideEncryptionConfigurationRule, BucketServerSideEncryptionConfigurationRuleApplyServerSideEncryptionByDefault, BucketWebsite}
import typings.pulumiAws.mod.route53.Record
import typings.pulumiAws.originAccessIdentityMod.OriginAccessIdentityArgs
import typings.pulumiAws.providerMod.ProviderArgs
import typings.pulumiAws.recordMod.RecordArgs
import typings.pulumiAws.recordTypeMod.RecordType
import typings.pulumiAws.{pulumiAwsStrings, mod => aws}
import typings.pulumiPulumi.invokeMod.InvokeOptions
import typings.pulumiPulumi.outputMod.Input
import typings.pulumiPulumi.{pulumiPulumiRequire, mod => pulumi}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportAll}
import scala.scalajs.js.|

//@JSExportAll //TODO: Exports all public members, maybe change to explicit exports?
object Main extends App {
  //Require Pulumi SDK
  pulumiPulumiRequire

  // Load the Pulumi program configuration. These act as the "parameters" to the Pulumi program,
  // so that different Pulumi Stacks can be brought up using the same code.
  val stackConfig = new pulumi.Config("static-website")

  // domainName is the domain/host to serve content at.
  val domainName = stackConfig.require[String]("domainName")

  //  // contentBucket is the S3 bucket that the website's contents will be stored in.
  val contentBucket = new aws.s3.Bucket("contentBucket",
    BucketArgs()
      .setBucket(domainName)
      .setAcl(CannedAcl.`private`)
      .setServerSideEncryptionConfiguration(
        BucketServerSideEncryptionConfiguration(
          BucketServerSideEncryptionConfigurationRule(
            BucketServerSideEncryptionConfigurationRuleApplyServerSideEncryptionByDefault("AES256")
          )
        )
      )
      // Configure S3 to serve bucket contents as a website. This way S3 will automatically convert
      // requests for "foo/" to "foo/index.html".
      .setWebsite(
        BucketWebsite()
          .setIndexDocument("index.html")
          .setErrorDocument("404.html")
      )
  )

  val bucketPublicAccessBlock = new aws.s3.BucketPublicAccessBlock("contentBucketPublicAccessBlock",
    BucketPublicAccessBlockArgs(
      bucket = contentBucket.id
    )
      .setBlockPublicAcls(true)
      .setBlockPublicPolicy(true)
      .setIgnorePublicAcls(true)
      .setRestrictPublicBuckets(true)
  )

  val certificate = new aws.acm.Certificate(s"$domainName-certificate",
    CertificateArgs()
      .setDomainName(domainName)
      .setSubjectAlternativeNamesVarargs(s"www.$domainName")
      .setValidationMethod("DNS"),
    awsUsEast1Provider
  )

  val topLevelDomain = domainName.split('.').takeRight(2).mkString(".")

  val hostedZoneId = aws.route53.getZone(
    GetZoneArgs()
      .setName(topLevelDomain),
    InvokeOptions()
      .setAsync(true)
  ).toFuture.map(_.zoneId).toJSPromise

  val tenMinutes = 10.minutes.toSeconds.toDouble

  /**
   * Create a DNS record to prove that we _own_ the domain we're requesting a certificate for.
   * See https://docs.aws.amazon.com/acm/latest/userguide/gs-acm-validate-dns.html for more info.
   */
  val certificateValidationDomains: Input[js.Array[Input[String]]] = certificate.domainValidationOptions.apply[js.Array[Input[String]]](_.map { domainValidationOption =>
    val record = new Record(s"${domainValidationOption.domainName}-validation",
      RecordArgs(
        domainValidationOption.resourceRecordName,
        domainValidationOption.resourceRecordType,
        hostedZoneId
      )
        .setRecordsVarargs(
          domainValidationOption.resourceRecordValue,
        )
        .setTtl(tenMinutes)
    )
    record.fqdn
  }.asInstanceOf[js.Array[Input[String]]])

  val originAccessIdentity = new aws.cloudfront.OriginAccessIdentity(
    "cloudFrontOriginAccessIdentity",
    OriginAccessIdentityArgs()
      .setComment(contentBucket.bucket)
  )

  val policyStatement: js.Array[Input[PolicyStatement]] = js.Array(
    PolicyStatement(pulumiAwsStrings.Allow)
      .setActionVarargs("s3:GetObject")
      .setResourceVarargs(contentBucket.arn[String](_ + "/*"))
      .setPrincipal(
        AWSPrincipal(js.Array(originAccessIdentity.iamArn).asInstanceOf[Input[js.Array[Input[String]] | String]])
      )
  )
  val readPolicy = new aws.s3.BucketPolicy("readPolicy",
    BucketPolicyArgs(contentBucket.id,
      PolicyDocument(
        policyStatement,
        pulumiAwsStrings.`2012-10-17`
      )
    )
  )


  /**
   * This is a _special_ resource that waits for ACM to complete validation via the DNS record
   * checking for a status of "ISSUED" on the certificate itself. No actual resources are
   * created (or updated or deleted).
   *
   * See https://www.terraform.io/docs/providers/aws/r/acm_certificate_validation.html for slightly more detail
   * and https://github.com/terraform-providers/terraform-provider-aws/blob/master/aws/resource_aws_acm_certificate_validation.go
   * for the actual implementation.
   */
  val certificateValidation = new aws.acm.CertificateValidation("certificateValidation",
    CertificateValidationArgs(certificate.arn)
      .setValidationRecordFqdns(certificateValidationDomains),
    awsUsEast1Provider
  )

  val methods: js.Array[Input[String]] = js.Array("GET", "HEAD")
  val origin: js.Array[Input[DistributionOrigin]] = js.Array(DistributionOrigin(contentBucket.bucketDomainName, s"s3-origin-$domainName")
    .setS3OriginConfig(
      DistributionOriginS3OriginConfig(originAccessIdentity.cloudfrontAccessIdentityPath)
    )
  )
  // distributionArgs configures the CloudFront distribution. Relevant documentation:
  // https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/distribution-web-values-specify.html
  // https://www.terraform.io/docs/providers/aws/r/cloudfront_distribution.html
  val distributionArgs = DistributionArgs(
    // A CloudFront distribution can configure different cache behaviors based on the request path.
    // Here we just specify a single, default cache behavior which is just read-only requests to S3.
    defaultCacheBehavior = DistributionDefaultCacheBehavior(
      methods,
      methods,
      DistributionDefaultCacheBehaviorForwardedValues(
        DistributionDefaultCacheBehaviorForwardedValuesCookies("none"),
        false
      ).setHeadersVarargs("Origin"),
      s"s3-origin-$domainName",
      "redirect-to-https"
    )
      .setCompress(true)
      .setMaxTtl(365.days.toSeconds.toDouble)
      .setMinTtl(30.days.toSeconds.toDouble)
      .setDefaultTtl(30.days.toSeconds.toDouble)
//      .setLambdaFunctionAssociationsVarargs(
//        DistributionDefaultCacheBehaviorLambdaFunctionAssociation(
//          "origin-request",
//          PathRewriter.pathRewriterArn
//        )
//      )
    ,
    enabled = true,
    // We only specify one origin for this distribution, the S3 content bucket.
    origins = origin,
    restrictions = DistributionRestrictions(DistributionRestrictionsGeoRestriction("none")),
    viewerCertificate = DistributionViewerCertificate()
      .setAcmCertificateArn(certificate.arn) // Per AWS, ACM certificate must be in the us-east-1 region.
      .setSslSupportMethod("sni-only")
  )
    // Alternate aliases the CloudFront distribution can be reached at, in addition to https://xxxx.cloudfront.net.
    // Required if you want to access the distribution via config.targetDomain as well.
    .setAliasesVarargs(domainName, s"www.$domainName")
    .setHttpVersion("http2")
    // You can customize error responses. When CloudFront receives an error from the origin (e.g. S3 or some other
    // web service) it can return a different error code, and return the response for a different resource.
    .setCustomErrorResponsesVarargs(
      DistributionCustomErrorResponse(403)
        .setResponseCode(200)
        .setResponsePagePath("/404.html")
    )
    // "All" is the most broad distribution, and also the most expensive.
    // "100" is the least broad, and also the least expensive.
    .setPriceClass("PriceClass_All")

  val distribution = new aws.cloudfront.Distribution("cdn", distributionArgs)
val babber = PathRewriter.pathRewriterArn
  // Creates a new Route53 DNS record pointing the domain to the CloudFront distribution.
  val route = cloudfrontAliasRecord(domainName, distribution)
  val wwwRoute = cloudfrontAliasRecord(s"www.$domainName", distribution)

  private def cloudfrontAliasRecord(domain: String, distribution: aws.cloudfront.Distribution) = {
    new Record(
      domain,
      RecordArgs(
        name = domain,
        `type` = RecordType.A,
        zoneId = hostedZoneId
      ).setAliasesVarargs(
        RecordAlias(
          evaluateTargetHealth = true,
          name = distribution.domainName,
          //ZoneId is always 'Z2FDTNDATAQYW2' for CloudFront
          zoneId = distribution.hostedZoneId
        )
      )
    )
  }

  //TODO: This didn't work
  // Export properties from this stack. This prints them at the end of `pulumi up` and
  // makes them easier to access from the pulumi.com.
  @JSExport
  val contentBucketUri = contentBucket.bucket[String]("s3://" + _)
  @JSExport
  val contentBucketWebsiteEndpoint = contentBucket.websiteEndpoint
  @JSExport
  val cloudFrontDomain = distribution.domainName
  @JSExport
  val targetDomainEndpoint = s"https://${domainName}/"

}
