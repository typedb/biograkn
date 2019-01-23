java_library(
    name = "metrics-core",
    exports = [
        "//external:jar/io/dropwizard/metrics/metrics-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "metrics-graphite",
    exports = [
        "//external:jar/io/dropwizard/metrics/metrics-graphite"
    ],
    runtime_deps = [
        ":metrics-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "metrics-json",
    exports = [
        "//external:jar/io/dropwizard/metrics/metrics-json"
    ],
    runtime_deps = [
        ":metrics-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "metrics-jvm",
    exports = [
        "//external:jar/io/dropwizard/metrics/metrics-jvm"
    ],
    runtime_deps = [
        ":metrics-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


