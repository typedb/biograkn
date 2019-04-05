java_library(
    name = "lucene-analyzers-common",
    exports = [
        "//external:jar/org/apache/lucene/lucene-analyzers-common"
    ],
    runtime_deps = [
        ":lucene-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "lucene-core",
    exports = [
        "//external:jar/org/apache/lucene/lucene-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "lucene-queries",
    exports = [
        "//external:jar/org/apache/lucene/lucene-queries"
    ],
    runtime_deps = [
        ":lucene-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "lucene-queryparser",
    exports = [
        "//external:jar/org/apache/lucene/lucene-queryparser"
    ],
    runtime_deps = [
        ":lucene-core",
        ":lucene-queries",
        ":lucene-sandbox"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "lucene-sandbox",
    exports = [
        "//external:jar/org/apache/lucene/lucene-sandbox"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


