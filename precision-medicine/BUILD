load("@graknlabs_rules_deployment//distribution:rules.bzl", "distribution_structure", "distribution_zip")

distribution_structure(
    name="distribution-structure",
    targets = {
        "//main": "services/lib/"
    },
    additional_files = {
        "//:run": "run",
    },
    permissions = {
      "server/services/cassandra/cassandra.yaml": "0755",
    }
)

distribution_zip(
    name = "distribution",
    distribution_structures = [":distribution-structure"],
    output_filename = "project-template-java",
)
