java_library(
    name = "migrator",
    srcs = glob(["*.java"]),
    visibility = [ "//visibility:public" ],
    deps = [
        "//dependencies/maven/artifacts/ai/grakn:client-java",
        "//dependencies/maven/artifacts/ai/grakn:grakn-core",
        "//dependencies/maven/artifacts/ai/grakn:grakn-graql",

        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv"
    ],
    data = ["//precision-medicine/datasets"]
)

java_test(
    name = "migrator-test",
    test_class = "grakn.template.java.migrator.test.MigratorTest",
    srcs = glob(["test/MigratorTest.java"]),
    deps = [ "//precision-medicine/main/migrator:migrator" ],
    visibility = [ "//visibility:public" ]
)