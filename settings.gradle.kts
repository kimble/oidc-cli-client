plugins {
    id("com.gradle.enterprise") version "3.10.1"
}

rootProject.name = "oidc-cli-client"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
