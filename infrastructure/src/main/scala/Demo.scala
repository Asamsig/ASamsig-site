import typings.pulumiAws.bucketMod.BucketArgs
import typings.pulumiAws.bucketPolicyMod.BucketPolicyArgs
import typings.pulumiAws.cannedAclMod.CannedAcl
import typings.pulumiAws.certificateMod.CertificateArgs
import typings.pulumiAws.certificateValidationMod.CertificateValidationArgs
import typings.pulumiAws.distributionMod.DistributionArgs
import typings.pulumiAws.documentsMod.{AWSPrincipal, PolicyDocument, PolicyStatement}
import typings.pulumiAws.getZoneMod.GetZoneArgs
import typings.pulumiAws.inputMod.cloudfront._
import typings.pulumiAws.inputMod.route53.RecordAlias
import typings.pulumiAws.inputMod.s3.{BucketServerSideEncryptionConfiguration, BucketServerSideEncryptionConfigurationRule, BucketServerSideEncryptionConfigurationRuleApplyServerSideEncryptionByDefault, BucketWebsite}
import typings.pulumiAws.originAccessIdentityMod.OriginAccessIdentityArgs
import typings.pulumiAws.providerMod.ProviderArgs
import typings.pulumiAws.recordMod.RecordArgs
import typings.pulumiAws.recordTypeMod.RecordType
import typings.pulumiAws.regionMod.Region
import typings.pulumiAws.{pulumiAwsStrings, mod => aws}
import typings.pulumiPulumi.invokeMod.InvokeOptions
import typings.pulumiPulumi.outputMod.Output_
import typings.pulumiPulumi.resourceMod.CustomResourceOptions
import typings.pulumiPulumi.{outputMod, pulumiPulumiRequire, mod => pulumi}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

//@JSExportAll //TODO: Exports all public members, maybe change to explicit exports?
object Demo extends App {
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
  //
  //  // crawlDirectory recursive crawls the provided directory, applying the provided function
  //  // to every file it contains. Doesn't handle cycles from symlinks.
  //  function crawlDirectory(dir: string, f: (_: string) => void) {
  //    val files = fs.readdirSync(dir)
  //    for (val file of files) {
  //      val filePath = `${dir}/${file}`
  //      val stat = fs.statSync(filePath)
  //      if (stat.isDirectory()) {
  //        crawlDirectory(filePath, f)
  //      }
  //      if (stat.isFile()) {
  //        f(filePath)
  //      }
  //    }
  //  }
  //
  //  // Sync the contents of the source directory with the S3 bucket, which will in-turn show up on the CDN.
  //  val webContentsRootPath = path.join(process.cwd(), config.pathToWebsiteContents)
  //  console.log("Syncing contents from local disk at", webContentsRootPath)
  //  crawlDirectory(
  //    webContentsRootPath,
  //    (filePath: string) => {
  //      val relativeFilePath = filePath.replace(webContentsRootPath + "/", "")
  //      val contentFile = new aws.s3.BucketObject(
  //        relativeFilePath,
  //        {
  //          key= relativeFilePath,
  //
  //          acl= "public-read",
  //          bucket= contentBucket,
  //          contentType= mime.getType(filePath) || undefined,
  //          source= new pulumi.asset.FileAsset(filePath),
  //        },
  //        {
  //          parent= contentBucket,
  //        })
  //    })
  //
  //  // logsBucket is an S3 bucket that will contain the CDN's request logs.
  //  val logsBucket = new aws.s3.Bucket("requestLogs",
  //    {
  //      bucket= `${config.targetDomain}-logs`,
  //      acl= "private",
  //    })
  //
  val tenMinutes = 60 * 10

  /**
   * Only provision a certificate (and related resources) if a certificateArn is _not_ provided via configuration.
   */
  val eastRegion = new aws.Provider("east", ProviderArgs()
    .setProfile(aws.config.profile.get) //TODO: Call .get??
    .setRegion(Region.`us-east-1`) // Per AWS, ACM certificate must be in the us-east-1 region.
  )
  //
  val certificate = new aws.acm.Certificate("certificate",
    CertificateArgs().
      setDomainName(domainName)
      .setSubjectAlternativeNamesVarargs(s"wwww.$domainName")
      .setValidationMethod("DNS"),
    CustomResourceOptions()
      .setProvider(eastRegion)
  )

  val hostedZoneId = aws.route53.getZone(
    GetZoneArgs()
      .setName(domainName),
    InvokeOptions()
      .setAsync(true)
  ).toFuture.map(_.zoneId).toJSPromise

//  private val value: typings.pulumiPulumi.outputMod.OutputInstance[String | typings.pulumiAws.recordTypeMod.RecordType] = certificate.domainValidationOptions.apply[String](validationOption => validationOption(0).resourceRecordType)
//  /**
//   * Create a DNS record to prove that we _own_ the domain we're requesting a certificate for.
//   * See https://docs.aws.amazon.com/acm/latest/userguide/gs-acm-validate-dns.html for more info.
//   */
//  val certificateValidationDomain = new aws.route53.Record(s"$domainName-validation",
//    RecordArgs(
//      certificate.domainValidationOptions.apply[String](validationOption => validationOption(0).resourceRecordName),
//      value,
//      hostedZoneId
//    )
//      .setRecords(certificate.domainValidationOptions.apply[js.Array[outputMod.Input[String]]](_.map(_.resourceRecordValue)))
//      .setTtl(10.minutes.toSeconds.toDouble)
//  )

  val originAccessIdentity = new aws.cloudfront.OriginAccessIdentity(
    "cloudFrontOriginAccessIdentity",
    OriginAccessIdentityArgs()
      .setComment(contentBucket.bucket)
  )

  private val s3CanonicalUserId: outputMod.Input[String] = originAccessIdentity.s3CanonicalUserId
  private val policyStatement: js.Array[outputMod.Input[PolicyStatement]] = js.Array(
    PolicyStatement(pulumiAwsStrings.Allow)
      .setActionVarargs("s3:GetObject")
      .setResourceVarargs(s"${contentBucket.arn}/*")
      .setPrincipal(
        AWSPrincipal(js.Array(s3CanonicalUserId))
      )
  )
  val readPolicy = new aws.s3.BucketPolicy("readPolicy",
    BucketPolicyArgs(contentBucket.bucket,
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
//  val certificateValidation = new aws.acm.CertificateValidation("certificateValidation",
//    CertificateValidationArgs(certificate.arn)
//      .setValidationRecordFqdnsVarargs(certificateValidationDomain.fqdn),
//    CustomResourceOptions()
//      .setProvider(eastRegion)
//  )

  private val methods: js.Array[outputMod.Input[String]] = js.Array("GET", "HEAD")
  private val origin: js.Array[outputMod.Input[DistributionOrigin]] = js.Array(DistributionOrigin(contentBucket.bucketDomainName, s"s3-origin-$domainName")
    .setS3OriginConfig(
      DistributionOriginS3OriginConfig(originAccessIdentity.id)
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
      .setDefaultTtl(30.days.toSeconds.toDouble),
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
        .setResponseCode(404)
        .setResponsePagePath("")
    )
    // "All" is the most broad distribution, and also the most expensive.
    // "100" is the least broad, and also the least expensive.
    .setPriceClass("PriceClass_All")
  //    defaultRootObject = "index.html",

  val distribution = new aws.cloudfront.Distribution("cdn", distributionArgs)

  // Creates a new Route53 DNS record pointing the domain to the CloudFront distribution.
  val route = new aws.route53.Record(
    domainName,
    RecordArgs(
      name = domainName,
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
  val wwwRoute = new aws.route53.Record(
    domainName,
    RecordArgs(
      name = s"wwww.$domainName",
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

  // Export properties from this stack. This prints them at the end of `pulumi up` and
  // makes them easier to access from the pulumi.com.
  @JSExport
  val contentBucketUri = s"s3://${contentBucket.bucket}"
  @JSExport
  val contentBucketWebsiteEndpoint = contentBucket.websiteEndpoint
  @JSExport
  val cloudFrontDomain = distribution.domainName
  @JSExport
  val targetDomainEndpoint = s"https://${domainName}/"

}
