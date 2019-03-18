workspace(name = "grakn_biograkn")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name="graknlabs_rules_deployment",
    remote="https://github.com/graknlabs/deployment",
    commit="8d68b4f13fe063ed7ccd04c29ab5f91e81fba052"
)

git_repository(
    name="graknlabs_grakn_core",
    remote="https://github.com/graknlabs/grakn",
    commit="b9ac28f854b06e8bb67483f9ff1e8b2f744d14b4"
)

git_repository(
    name="graknlabs_bazel_distribution",
    remote="https://github.com/graknlabs/bazel-distribution",
    commit="3fff34b151afabaee5af7ffb35ed99e52747c932"
)

git_repository(
    name = "graknlabs_client_java",
    remote = "https://github.com/graknlabs/client-java.git",
    commit = "bbc8e2eaf99f8e2ecb4fe06813a47dcb36f96071"
)
load("@graknlabs_client_java//dependencies/maven:dependencies.bzl", maven_dependencies_for_build= "maven_dependencies")
maven_dependencies_for_build()

# ----- @graknlabs_grakn deps -----

load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl", maven_dependencies_for_build = "maven_dependencies")
maven_dependencies_for_build()

# Load Graql dependencies
load("@graknlabs_grakn_core//dependencies/git:dependencies.bzl", "graknlabs_graql")
graknlabs_graql()

# Load ANTLR dependencies for Bazel
load("@graknlabs_graql//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
antlr_dependencies()

# Load ANTLR dependencies for ANTLR programs
load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()

load("@graknlabs_graql//dependencies/maven:dependencies.bzl", graql_dependencies = "maven_dependencies")
graql_dependencies()

load("@graknlabs_grakn_core//dependencies/docker:dependencies.bzl", "docker_dependencies")
docker_dependencies()

load("@graknlabs_grakn_core//dependencies/compilers:dependencies.bzl", "grpc_dependencies")
grpc_dependencies()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", com_github_grpc_grpc_bazel_grpc_deps = "grpc_deps")
com_github_grpc_grpc_bazel_grpc_deps()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()

load("//dependencies/tools:dependencies.bzl", "tools_dependencies")
tools_dependencies()

load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
maven_dependencies()

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")


git_repository(
 name="com_github_google_bazel_common",
 remote="https://github.com/graknlabs/bazel-common",
 commit="550f0490798a4e4b6c5ff8cac3b6f5c2a5e81e21",
)
load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()




http_archive(
  name = "bazel_toolchains",
  sha256 = "07a81ee03f5feae354c9f98c884e8e886914856fb2b6a63cba4619ef10aaaf0b",
  strip_prefix = "bazel-toolchains-31b5dc8c4e9c7fd3f5f4d04c6714f2ce87b126c1",
  urls = [
    "https://mirror.bazel.build/github.com/bazelbuild/bazel-toolchains/archive/31b5dc8c4e9c7fd3f5f4d04c6714f2ce87b126c1.tar.gz",
    "https://github.com/bazelbuild/bazel-toolchains/archive/31b5dc8c4e9c7fd3f5f4d04c6714f2ce87b126c1.tar.gz",
  ],
)