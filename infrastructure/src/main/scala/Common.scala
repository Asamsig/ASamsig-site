import typings.pulumiAws.providerMod.ProviderArgs
import typings.pulumiAws.regionMod.Region
import typings.pulumiAws.{mod => aws}
import typings.pulumiPulumi.resourceMod.CustomResourceOptions

object Common {
  /**
   * Some resources _must_ be put in us-east-1, such as Lambda at Edge.
   */
  val eastRegion = new aws.Provider("east", ProviderArgs()
    .setProfile(aws.config.profile.get) //TODO: Call .get??
    .setRegion(Region.`us-east-1`) // Per AWS, ACM certificate must be in the us-east-1 region.
  )

  /**
   * CustomResourceOption with provider set to us-east-1
   */
  val awsUsEast1Provider = CustomResourceOptions()
    .setProvider(eastRegion)

}
