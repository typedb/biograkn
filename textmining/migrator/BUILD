package(default_visibility = ["//visibility:public"])

java_library(
    name = "migrator",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "**/test/**",
            "*Test.java",
        ],
    ),
    data = [
        "//textmining/dataset",
        "//textmining/schema",
    ],
    deps = [
        "//dependencies/maven/artifacts/edu/stanford/nlp:stanford-corenlp",
        "//dependencies/maven/artifacts/edu/stanford/nlp:stanford-corenlp-java-models",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//utils",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
    ],
)

#java_test(
#      name = "migrator-test",
#      test_class = "grakn.biograkn.migrator.MigratorTest",
#      srcs = ["MigratorTest.java"],
#      deps = [":migrator"],
#  )
#
