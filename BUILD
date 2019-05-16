
exports_files(["VERSION"])

load("@graknlabs_bazel_distribution//packer:rules.bzl", "deploy_packer")
load("@graknlabs_bazel_distribution//gcp:rules.bzl", "assemble_gcp")


assemble_gcp(
    name = "assemble-gcp-snapshot",
    files = {
#        "//dist:biograkn-linux.tar.gz": "biograkn-linux.tar.gz",
#        ":assemble-linux-targz": "grakn-kgms-all-linux.tar.gz",
#        "//deployment/common:cassandra-env.sh" : "cassandra-env.sh",
#        "//deployment/common:default-grakn-environmentfile" : "default-grakn-environmentfile",
#        "//deployment/common:grakn-properties-and-service-config.yml" : "grakn-properties-and-service-config.yml",
#        "//deployment/common:grakn.properties.j2" : "grakn.properties.j2",
#        "//deployment/common:grakn.service.j2" : "grakn.service.j2",
#        "//deployment/deploy-gcp-image/files:oss-licenses.zip" : "oss-licenses.zip",
#        "//deployment/deploy-gcp-image/files:oss-src.zip" : "oss-src.zip",
#        "//deployment/deploy-gcp-image/files:shutdown-script.sh" : "shutdown-script.sh",
#        "//deployment/deploy-gcp-image/files:startup-script.sh" : "startup-script.sh",
    },
    install = "//deployment/gcp/files:install.sh",
    project_id = "grakn-dev",
    zone = "europe-west1-b",
    image_name = "biograkn-snapshot-{{user `version`}}",
    image_licenses = 'projects/grakn-public/global/licenses/grakn-kgms-premium'
)

deploy_packer(
     name = "deploy-gcp-snapshot",
     target = ":assemble-gcp-snapshot",
 )