java_library(
    name = "avro",
    exports = [
        "//external:jar/org/apache/avro/avro"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/thoughtworks/paranamer:paranamer",
        "//dependencies/maven/artifacts/org/apache/commons:commons-compress"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "avro-ipc",
    exports = [
        "//external:jar/org/apache/avro/avro-ipc"
    ],
    runtime_deps = [
        ":avro"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "avro-ipc-jar-tests",
    exports = [
        "//external:jar/org/apache/avro/avro-ipc-jar-tests"
    ],
    runtime_deps = [
        ":avro"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "avro-mapred-jar-hadoop2",
    exports = [
        "//external:jar/org/apache/avro/avro-mapred-jar-hadoop2"
    ],
    runtime_deps = [
        ":avro-ipc",
        ":avro-ipc-jar-tests"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


