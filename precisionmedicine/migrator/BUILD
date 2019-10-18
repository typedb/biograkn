
package(default_visibility = [ "//visibility:public" ])

java_library(
    name = "migrator",
    srcs = glob(["**/*.java"], exclude = ["**/*Test.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_graql//java:graql",

        "//dependencies/maven/artifacts/org/apache/poi:poi-ooxml",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/edu/stanford/nlp:stanford-corenlp",
        "//dependencies/maven/artifacts/edu/stanford/nlp:stanford-corenlp-java-models",

        "//utils:utils"
    ],
    data = [
        "//precisionmedicine/dataset",
        "//precisionmedicine/schema"
    ],
)
