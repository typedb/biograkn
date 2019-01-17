java_library(
    name = "migrator",
    srcs = glob(["*.java"]),
    visibility = [ "//visibility:public" ],
    deps = [
        "//dependencies/maven/artifacts/ai/grakn:client-java",
        "//dependencies/maven/artifacts/ai/grakn:grakn-core",
        "//dependencies/maven/artifacts/ai/grakn:grakn-graql"
    ]
)

java_test(
    name = "migrator-test",
    test_class = "grakn.template.java.MigratorTest",
    srcs = glob(["test/MigratorTest.java"]),
    deps = [ "//main/migrator:migrator" ],
    visibility = [ "//visibility:public" ]
)