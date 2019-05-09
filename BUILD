
exports_files(["VERSION"])

deploy_github(
    name = "deploy-github",
    deployment_properties = "//:deployment.properties",
    release_description = "//:RELEASE_TEMPLATE.md",
    archive = ":assemble-versioned-all",
    version_file = "//:VERSION"
)