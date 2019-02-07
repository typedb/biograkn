
package(default_visibility = [ "//visibility:public" ])
load("@graknlabs_rules_deployment//distribution:rules.bzl", "distribution_structure", "distribution_zip")

java_library(
    name = "migrator",
    srcs = glob(["**/*.java"], exclude = ["**/test/**", "*Test.java"]),
    deps = [
        "//dependencies/maven/artifacts/ai/grakn:client-java",
        "//dependencies/maven/artifacts/ai/grakn:grakn-core",
        "//dependencies/maven/artifacts/ai/grakn:grakn-graql",

        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv"
    ],
    data = ["//dataset"]
)

java_binary(
    name = "migrator-bin",
    runtime_deps = [":migrator"],
    main_class = "grakn.biograkn.migrator.Migrator"
)


distribution_structure(
    name = "distribution-structure",
    targets = {
        "//migrator:migrator": "services/lib/",
    },
    additional_files = {
        "//migrator:migrate": "migrate",
    }
)

distribution_zip(
    name = "distribution",
    distribution_structures = [":distribution-structure"],
    output_filename = "biograkn-migrator",
)

#java_test(
  ##    name = "migrator-test",
  ##    test_class = "grakn.biograkn.migrator.MigratorTest",
  ##    srcs = ["MigratorTest.java"],
  ##    deps = [":migrator"],
  ##)
  ##
  ##java_test(
  ##    name = "person-test",
  ##    test_class = "grakn.biograkn.migrator.person.test.PersonTest",
  ##    srcs = glob(["person/test/PersonTest.java"]),
  ##    deps = [ ":migrator" ],
  ##)