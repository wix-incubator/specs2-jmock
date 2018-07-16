# Publishing to Maven Central

This project publishing is not automated. Everything is executed manually from a developer machine.

## Step 1. Prerequisites

1. You need a OSSRH account with rights to release `com.wix` artifacts. Credentials are excpected to be available under environment variables `SONATYPE_USERNAME` and `SONATYPE_PASSWORD`.
2. Additionally, you need a valid PGP key for your account. This project uses sbt-pgp plugin, usage can be found here: https://github.com/sbt/sbt-pgp/wiki/Using-GPG .

## Step 2. Publishing

Since process is not automated, you must manually change versions before and after publishing artifacts. Please commit the changes.
 
Publishing itself is done by executing following command:

```sh
sbt +publishSigned
```

## Step 3. Releasing

After publishing artifacts successfully, they will be visible (if you have rights) in https://oss.sonatype.org/#stagingRepositories . Make sure content looks OK, then close and release it. Usually, artifacts become available in a couple of hours. If not, wait at least 24 hours before panicking.

Refer to https://central.sonatype.org/pages/ossrh-guide.html for more detailed information.
