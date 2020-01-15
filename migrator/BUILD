package(default_visibility = ["//visibility:public"])

load("@graknlabs_bazel_distribution//common:rules.bzl", "assemble_zip", "java_deps")

java_library(
    name = "migrator",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "**/test/**",
            "*Test.java",
        ],
    ),
    deps = [
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//precisionmedicine/migrator",
        "//textmining/migrator",
    ],
)

java_binary(
    name = "migrator-bin",
    classpath_resources = ["//conf:logback"],
    main_class = "grakn.biograkn.migrator.Migrator",
    runtime_deps = [":migrator"],
)

# For mock assembly
java_binary(
    name = "migrator-mock",
    classpath_resources = ["//conf:logback"],
    main_class = "grakn.biograkn.migrator.MigratorMock",
    runtime_deps = [":migrator"],
)

java_deps(
    name = "distribution-structure",
    java_deps_root = "services/lib/",
    target = ":migrator-bin",
    version_file = "//:VERSION",
)

assemble_zip(
    name = "assemble-mac-zip",
    additional_files = {
        "//migrator:migrate": "migrate",
    },
    output_filename = "biograkn-migrator",
    targets = [":distribution-structure"],
)

java_test(
    name = "assemble-mock-test",
    srcs = ["AssembleMockTest.java"],
    test_class = "grakn.biograkn.migrator.AssembleMockTest",
    deps = [
        "//precisionmedicine/migrator",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
    ],
)

java_test(
    name = "assemble-all-test",
    srcs = ["AssembleAllTest.java"],
    test_class = "grakn.biograkn.migrator.AssembleAllTest",
    deps = [
        "//precisionmedicine/migrator",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
    ],
)
