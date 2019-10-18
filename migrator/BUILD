
package(default_visibility = [ "//visibility:public" ])
load("@graknlabs_bazel_distribution//common:rules.bzl", "java_deps", "assemble_zip")

java_library(
    name = "migrator",
    srcs = glob(["**/*.java"], exclude = ["**/test/**", "*Test.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_graql//java:graql",

        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",

        "//precisionmedicine/migrator",
        "//textmining/migrator"
    ],
)

java_binary(
    name = "migrator-bin",
    runtime_deps = [":migrator"],
    main_class = "grakn.biograkn.migrator.Migrator",
    classpath_resources = ["//conf:logback"]
)

# For mock assembly
java_binary(
    name = "migrator-mock",
    runtime_deps = [":migrator"],
    main_class = "grakn.biograkn.migrator.MigratorMock",
    classpath_resources = ["//conf:logback"]
)

java_deps(
    name = "distribution-structure",
    target = ":migrator-bin",
    java_deps_root = "services/lib/",
    version_file = "//:VERSION"
)

assemble_zip(
    name = "assemble-mac-zip",
    targets = [":distribution-structure"],
    additional_files = {
        "//migrator:migrate": "migrate",
    },
    output_filename = "biograkn-migrator",
)

java_test(
    name = "assemble-mock-test",
    test_class = "grakn.biograkn.migrator.AssembleMockTest",
    srcs = ["AssembleMockTest.java"],
    deps = [
        "//precisionmedicine/migrator",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_graql//java:graql",
    ],
  )

java_test(
    name = "assemble-all-test",
    test_class = "grakn.biograkn.migrator.AssembleAllTest",
    srcs = ["AssembleAllTest.java"],
    deps = [
        "//precisionmedicine/migrator",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_graql//java:graql",
    ],
  )

