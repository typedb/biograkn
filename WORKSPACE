#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

workspace(name = "graknlabs_biograkn")

###########################
# Grakn Labs dependencies #
###########################

load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_grakn_core", "graknlabs_build_tools", "graknlabs_client_java", "graknlabs_client_python")
graknlabs_grakn_core()
graknlabs_build_tools()
graknlabs_client_java()
graknlabs_client_python()

load("@graknlabs_build_tools//distribution:dependencies.bzl", "graknlabs_bazel_distribution")
graknlabs_bazel_distribution()

###########################
# Load Bazel dependencies #
###########################

load("@graknlabs_build_tools//bazel:dependencies.bzl", "bazel_common", "bazel_deps", "bazel_toolchain")
bazel_common()
bazel_deps()
bazel_toolchain()

#################################
# Load Build Tools dependencies #
#################################

load("@graknlabs_build_tools//checkstyle:dependencies.bzl", "checkstyle_dependencies")
checkstyle_dependencies()

load("@graknlabs_build_tools//bazel:dependencies.bzl", "bazel_rules_python")
bazel_rules_python()

load("@io_bazel_rules_python//python:pip.bzl", "pip_repositories", "pip_import")
pip_repositories()

pip_import(
    name = "graknlabs_build_tools_ci_pip",
    requirements = "@graknlabs_build_tools//ci:requirements.txt",
)
load("@graknlabs_build_tools_ci_pip//:requirements.bzl",
graknlabs_build_tools_ci_pip_install = "pip_install")
graknlabs_build_tools_ci_pip_install()

load("@graknlabs_build_tools//unused_deps:dependencies.bzl", "unused_deps_dependencies")
unused_deps_dependencies()

#####################################
# Load Java dependencies from Maven #
#####################################

load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
maven_dependencies()

##########################
# Load GRPC dependencies #
##########################

load("@graknlabs_build_tools//grpc:dependencies.bzl", "grpc_dependencies")
grpc_dependencies()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()

################################
# Load Grakn Core dependencies #
################################

load("@graknlabs_grakn_core//dependencies/graknlabs:dependencies.bzl",
"graknlabs_graql", "graknlabs_protocol", "graknlabs_common",
"graknlabs_console", "graknlabs_benchmark")
graknlabs_graql()
graknlabs_protocol()
graknlabs_common()
graknlabs_console()
graknlabs_benchmark()

load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl",
graknlabs_grakn_core_maven_dependencies = "maven_dependencies")
graknlabs_grakn_core_maven_dependencies()

load("@graknlabs_benchmark//dependencies/maven:dependencies.bzl",
graknlabs_benchmark_maven_dependencies = "maven_dependencies")
graknlabs_benchmark_maven_dependencies()

load("@graknlabs_build_tools//bazel:dependencies.bzl", "bazel_rules_docker")
bazel_rules_docker()

###########################
# Load Graql dependencies #
###########################

load("@graknlabs_graql//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
antlr_dependencies()

load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()

load("@graknlabs_graql//dependencies/maven:dependencies.bzl",
graknlabs_graql_maven_dependencies = "maven_dependencies")
graknlabs_graql_maven_dependencies()

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


##################################
# Load Distribution dependencies #
##################################

# TODO: rename the macro we load here to deploy_github_dependencies
load("@graknlabs_bazel_distribution//github:dependencies.bzl", "tcnksm_ghr")
tcnksm_ghr()

load("@graknlabs_bazel_distribution//common:dependencies.bzl", "bazelbuild_rules_pkg")
bazelbuild_rules_pkg()

load("@graknlabs_bazel_distribution//packer:dependencies.bzl", "deploy_packer_dependencies")
deploy_packer_dependencies()


#####################################
# Load Bazel common workspace rules #
#####################################

# TODO: Figure out why this cannot be loaded at earlier at the top of the file
load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()

# Generate a JSON document of commit hashes of all external workspace dependencies
load("@graknlabs_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "graknlabs_biograkn_workspace_refs"
)
