workspace(name = "grakn_biograkn")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# ----- @graknlabs_grakn -----


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


# ----- @graknlabs_client_java -----

git_repository(
    name = "graknlabs_client_java",
    remote = "https://github.com/graknlabs/client-java.git",
    commit = "bbc8e2eaf99f8e2ecb4fe06813a47dcb36f96071"
)

# ----- @graknlabs_client_java deps-----

load("@graknlabs_client_java//dependencies/maven:dependencies.bzl", maven_dependencies_for_build= "maven_dependencies")
maven_dependencies_for_build()


# ----- From BLAST WORKSPACE -----

################################
# Load Grakn Labs Dependencies #
################################

load("//dependencies/graknlabs:dependencies.bzl",
     "graknlabs_grakn_core", "graknlabs_client_python", "graknlabs_build_tools")
graknlabs_grakn_core()
graknlabs_client_python()
graknlabs_build_tools()

load("@graknlabs_build_tools//distribution:dependencies.bzl", "graknlabs_bazel_distribution")
graknlabs_bazel_distribution()


###########################
# Load Bazel Dependencies #
###########################

# Load additional build tools, such bazel-deps and unused-deps
load("@graknlabs_build_tools//bazel:dependencies.bzl",
     "bazel_common", "bazel_deps", "bazel_toolchain", "bazel_rules_python")
bazel_common()
bazel_deps()
bazel_toolchain()
bazel_rules_python()

load("@io_bazel_rules_python//python:pip.bzl", "pip_repositories", "pip_import")
pip_repositories()


#################################
# Load Build Tools Dependencies #
#################################

pip_import(
    name = "graknlabs_build_tools_ci_pip",
    requirements = "@graknlabs_build_tools//ci:requirements.txt",
)
load("@graknlabs_build_tools_ci_pip//:requirements.bzl",
graknlabs_build_tools_ci_pip_install = "pip_install")
graknlabs_build_tools_ci_pip_install()

pip_import(
    name = "graknlabs_bazel_distribution_pip",
    requirements = "@graknlabs_bazel_distribution//pip:requirements.txt",
)
load("@graknlabs_bazel_distribution_pip//:requirements.bzl",
graknlabs_bazel_distribution_pip_install = "pip_install")
graknlabs_bazel_distribution_pip_install()


###########################
# Load Local Dependencies #
###########################

# for Python

pip_import(
    name = "blast_example_pip",
    requirements = "//blast:requirements.txt",
)

load("@blast_example_pip//:requirements.bzl",
test_example_pip_install = "pip_install")
test_example_pip_install()


##########################
# Load GRPC Dependencies #
##########################

load("@graknlabs_build_tools//grpc:dependencies.bzl", "grpc_dependencies")
grpc_dependencies()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()


###################################
# Load Client Python Dependencies #
###################################

pip_import(
    name = "graknlabs_client_python_pip",
    requirements = "@graknlabs_client_python//:requirements.txt",
)

load("@graknlabs_client_python_pip//:requirements.bzl",
graknlabs_client_python_pip_install = "pip_install")
graknlabs_client_python_pip_install()


#####################################
# Load Bazel Common Workspace Rules #
#####################################

# TODO: Figure out why this cannot be loaded at earlier at the top of the file
#load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
#google_common_workspace_rules()
