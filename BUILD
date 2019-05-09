
exports_files(["VERSION"])

load("@graknlabs_bazel_distribution//github:rules.bzl", "deploy_github")

deploy_github(
    name = "deploy-github",
    deployment_properties = "//:deployment.properties",
    release_description = "//:RELEASE_TEMPLATE.md",
    archive = ":assemble-versioned-all",
    version_file = "//:VERSION"
)