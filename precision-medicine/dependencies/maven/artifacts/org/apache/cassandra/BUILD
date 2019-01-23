java_library(
    name = "cassandra-all",
    exports = [
        "//external:jar/org/apache/cassandra/cassandra-all"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/ch/qos/logback:logback-core",
        "//dependencies/maven/artifacts/com/addthis/metrics:reporter-config",
        "//dependencies/maven/artifacts/com/clearspring/analytics:stream",
        "//dependencies/maven/artifacts/com/github/jbellis:jamm",
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/com/googlecode/concurrentlinkedhashmap:concurrentlinkedhashmap-lru",
        "//dependencies/maven/artifacts/com/googlecode/json-simple:json-simple",
        "//dependencies/maven/artifacts/com/ning:compress-lzf",
        "//dependencies/maven/artifacts/com/thinkaurelius/thrift:thrift-server",
        "//dependencies/maven/artifacts/com/yammer/metrics:metrics-core",
        "//dependencies/maven/artifacts/commons-cli:commons-cli",
        "//dependencies/maven/artifacts/commons-codec:commons-codec",
        "//dependencies/maven/artifacts/io/netty:netty-all",
        "//dependencies/maven/artifacts/jline:jline",
        "//dependencies/maven/artifacts/net/java/dev/jna:jna",
        "//dependencies/maven/artifacts/net/jpountz/lz4:lz4",
        "//dependencies/maven/artifacts/net/sf/supercsv:super-csv",
        "//dependencies/maven/artifacts/org/antlr:antlr",
        "//dependencies/maven/artifacts/org/antlr:antlr-runtime",
        "//dependencies/maven/artifacts/org/apache/commons:commons-lang3",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",
        "//dependencies/maven/artifacts/org/apache/thrift:libthrift",
        "//dependencies/maven/artifacts/org/codehaus/jackson:jackson-core-asl",
        "//dependencies/maven/artifacts/org/codehaus/jackson:jackson-mapper-asl",
        "//dependencies/maven/artifacts/org/mindrot:jbcrypt",
        "//dependencies/maven/artifacts/org/xerial/snappy:snappy-java",
        "//dependencies/maven/artifacts/org/yaml:snakeyaml",
        ":cassandra-thrift"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "cassandra-thrift",
    exports = [
        "//external:jar/org/apache/cassandra/cassandra-thrift"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/apache/commons:commons-lang3",
        "//dependencies/maven/artifacts/org/apache/thrift:libthrift"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


