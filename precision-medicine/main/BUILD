java_library(
    name = "main",
    srcs = glob(["*.java"]),
    deps = [
        "//precision-medicine/common",
        "//precision-medicine/main/migrator:migrator",
        "//dependencies/maven/artifacts/com/google/guava",

        "//dependencies/maven/artifacts/ai/grakn:client-java",
        "//dependencies/maven/artifacts/ai/grakn:grakn-core",
        "//dependencies/maven/artifacts/ai/grakn:grakn-graql"
    ],
    visibility = ["//visibility:public"]
)

java_test(
    name = "main-test",
    test_class = "grakn.template.java.MainTest",
    srcs = ["test/MainTest.java"],
    deps = ["//precision-medicine/main"],
    data = ["//precision-medicine:distribution"]
)

java_binary(
    name = "main-bin",
    runtime_deps = [":main"],
    main_class = "grakn.template.java.Main"
)