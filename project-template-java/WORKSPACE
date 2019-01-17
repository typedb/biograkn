workspace(name = "project_template_java")

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("//dependencies/tools:dependencies.bzl", "tools_dependencies")
load("//dependencies/maven:dependencies.bzl", "maven_dependencies")

git_repository(
    name="graknlabs_rules_deployment",
    remote="https://github.com/graknlabs/deployment",
    commit="8d68b4f13fe063ed7ccd04c29ab5f91e81fba052"
)

tools_dependencies()

maven_dependencies()
