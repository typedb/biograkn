java_library(
    name = "poi",
    exports = [
        "//external:jar/org/apache/poi/poi"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/commons-codec:commons-codec",
        "//dependencies/maven/artifacts/org/apache/commons:commons-collections4",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "poi-ooxml",
    exports = [
        "//external:jar/org/apache/poi/poi-ooxml"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/github/virtuald:curvesapi",
        "//dependencies/maven/artifacts/org/apache/commons:commons-compress",
        ":poi",
        ":poi-ooxml-schemas"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "poi-ooxml-schemas",
    exports = [
        "//external:jar/org/apache/poi/poi-ooxml-schemas"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/apache/xmlbeans:xmlbeans"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


