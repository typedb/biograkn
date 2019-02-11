workspace(name = "precision_medicine")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("//dependencies/tools:dependencies.bzl", "tools_dependencies")
load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

git_repository(
    name="graknlabs_rules_deployment",
    remote="https://github.com/graknlabs/deployment",
    commit="8d68b4f13fe063ed7ccd04c29ab5f91e81fba052"
)

tools_dependencies()

maven_dependencies()

http_archive(
  name = "bazel_toolchains",
  sha256 = "07a81ee03f5feae354c9f98c884e8e886914856fb2b6a63cba4619ef10aaaf0b",
  strip_prefix = "bazel-toolchains-31b5dc8c4e9c7fd3f5f4d04c6714f2ce87b126c1",
  urls = [
    "https://mirror.bazel.build/github.com/bazelbuild/bazel-toolchains/archive/31b5dc8c4e9c7fd3f5f4d04c6714f2ce87b126c1.tar.gz",
    "https://github.com/bazelbuild/bazel-toolchains/archive/31b5dc8c4e9c7fd3f5f4d04c6714f2ce87b126c1.tar.gz",
  ],
)