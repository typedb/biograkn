
exports_files(["VERSION"])

load("@graknlabs_bazel_distribution//packer:rules.bzl", "deploy_packer")
load("@graknlabs_bazel_distribution//gcp:rules.bzl", "assemble_gcp")


assemble_gcp(
    name = "assemble-gcp-snapshot",
    files = {
        ":assemble-linux-tarz": "biograkn-linux.tar.gz",
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

genrule(
    name = "assemble-mac-zip",
    srcs = [
        "@graknlabs_grakn_core//:assemble-mac-zip",
         "//migrator:migrator-bin",
         "assemble-mac-zip.py"
    ],
    cmd = """
        python $(location assemble-mac-zip.py) $(location @graknlabs_grakn_core//:assemble-mac-zip) "$(locations //migrator:migrator-bin)" $@
    """,
    outs = ["biograkn-mac.zip"]
)

genrule(
    name = "assemble-mac-mock-zip",
    srcs = [
        "@graknlabs_grakn_core//:assemble-mac-zip",
         "//migrator:migrator-mock",
         "assemble-mac-zip.py"
    ],
    cmd = """
        python $(location assemble-mac-zip.py) $(location @graknlabs_grakn_core//:assemble-mac-zip) "$(locations //migrator:migrator-mock)" $@
    """,
    outs = ["biograkn-mac-mock.zip"]
)

genrule(
    name = "assemble-linux-tarz",
    srcs = [
        "@graknlabs_grakn_core//:assemble-linux-targz",
         "//migrator:migrator-bin",
         "assemble-linux-tarz.py"
    ],
    cmd = """
        python $(location assemble-linux-tarz.py) $(location @graknlabs_grakn_core//:assemble-linux-targz) "$(locations //migrator:migrator-bin)" $@
    """,
    outs = ["biograkn-linux.tar.gz"]
)

genrule(
    name = "assemble-linux-mock-tarz",
    srcs = [
        "@graknlabs_grakn_core//:assemble-linux-targz",
         "//migrator:migrator-mock",
         "assemble-linux-tarz.py"
    ],
    cmd = """
        python $(location assemble-linux-tarz.py) $(location @graknlabs_grakn_core//:assemble-linux-targz) "$(locations //migrator:migrator-mock)" $@
    """,
    outs = ["biograkn-linux-mock.tar.gz"]
)







