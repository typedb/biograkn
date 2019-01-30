
package(default_visibility = [ "//visibility:public" ])

java_library(
    name = "migrator",
    srcs = glob(["**/*.java"], exclude = ["**/test/**"]),
    deps = [
        "//dependencies/maven/artifacts/ai/grakn:client-java",
        "//dependencies/maven/artifacts/ai/grakn:grakn-core",
        "//dependencies/maven/artifacts/ai/grakn:grakn-graql",

        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv"
    ],
    data = ["//dataset"]
)

java_test(
    name = "person-test",
    test_class = "grakn.biograkn.migrator.person.test.PersonTest",
    srcs = glob(["person/test/PersonTest.java"]),
    deps = [ ":migrator" ],
)