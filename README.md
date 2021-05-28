# ASamsig-site
This site is a static website created with [Scala.js](https://www.scala-js.org/) and [Slinky](https://slinky.dev), built based on the [Slinky docs](https://github.com/shadaj/slinky/tree/master/docs).

I build this site to experiment with Scala.js, Slinky (React), Github Actions, static website hosting, AWS S3 and CloudFront.

## Working on project
Start SBT in root folder, switch to one of the sub-modules with `project <projectname>`, these sub-modules exist:
 - infrastructure - This is the Pulumi stack to create the infrastructure in AWS
   - Developing in his module, requires you to run `~fastOptJS::webpack"`. To then check the infrastructure changes, change directory to the infrastructure-folder and run `pulumi pre`
 - lambdaPathRewriter - This is a small lambda, that is used to strip the url paths of `.html` 
   - Developing in his module, requires you to run `~fastOptJS::webpack"`.
 - site - This is the actual code for the site
   - Developing in his module, requires you to run `dev`. You can then access the website at http://localhost:8080

