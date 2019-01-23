java_library(
    name = "client-java",
    exports = [
        "//external:jar/ai/grakn/client-java"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/github/rholder:guava-retrying",
        "//dependencies/maven/artifacts/com/netflix/hystrix:hystrix-core",
        "//dependencies/maven/artifacts/com/sun/jersey:jersey-client",
        "//dependencies/maven/artifacts/io/dropwizard/metrics:metrics-core",
        "//dependencies/maven/artifacts/io/grpc:grpc-netty",
        "//dependencies/maven/artifacts/io/grpc:grpc-protobuf",
        "//dependencies/maven/artifacts/io/grpc:grpc-stub",
        "//dependencies/maven/artifacts/io/netty:netty-all",
        "//dependencies/maven/artifacts/net/jodah:failsafe",
        "//dependencies/maven/artifacts/org/apache/httpcomponents:httpclient",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-simple",
        ":grakn-core",
        ":grakn-graql"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grakn-core",
    exports = [
        "//external:jar/ai/grakn/grakn-core"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-annotations",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-core",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-databind",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/module:jackson-module-scala-2-11",
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/io/reactivex:rxjava",
        "//dependencies/maven/artifacts/org/apache/httpcomponents:httpclient",
        "//dependencies/maven/artifacts/org/apache/tinkerpop:gremlin-core",
        "//dependencies/maven/artifacts/org/apache/tinkerpop:tinkergraph-gremlin",
        "//dependencies/maven/artifacts/org/sharegov:mjson",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grakn-graql",
    exports = [
        "//external:jar/ai/grakn/grakn-graql"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/antlr:antlr4-runtime",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        ":grakn-core",
        ":grakn-kb"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "grakn-kb",
    exports = [
        "//external:jar/ai/grakn/grakn-kb"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/org/apache/tinkerpop:hadoop-gremlin",
        "//dependencies/maven/artifacts/org/apache/tinkerpop:spark-gremlin",
        "//dependencies/maven/artifacts/org/janusgraph:janusgraph-cassandra",
        "//dependencies/maven/artifacts/org/janusgraph:janusgraph-hadoop",
        ":grakn-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


