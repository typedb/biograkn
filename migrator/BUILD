
package(default_visibility = [ "//visibility:public" ])
load("@graknlabs_rules_deployment//distribution:rules.bzl", "distribution_structure", "distribution_zip")

java_library(
    name = "migrator",
    srcs = glob(["**/*.java"], exclude = ["**/test/**", "*Test.java"]),
    deps = [
        "@graknlabs_grakn_core//client-java:client-java",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_graql//java:graql",

        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/edu/stanford/nlp:stanford-corenlp",
        "//dependencies/maven/artifacts/edu/stanford/nlp:stanford-corenlp-java-models"
    ],
#    data = ["//dataset"],
)



java_binary(
    name = "migrator-bin",
    runtime_deps = [":migrator"],
    main_class = "grakn.biograkn.migrator.Migrator",
    classpath_resources = ["//conf:logback"]
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

java_test(
      name = "migrator-test",
      test_class = "grakn.biograkn.migrator.MigratorTest",
      srcs = ["MigratorTest.java"],
      deps = [":migrator"],
  )

