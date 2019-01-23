Pre-configured template for Java development, containing the following basic components:
1. a Java project skeleton using Bazel
2. The necessary CI workflows which defines the skeleton for tests, distribution tests, and release deployments

# Requirements
- Java
- CircleCI accounts
- Bazel
  - (optional) RBE credential set up in CircleCI
- (optional) [IntelliJ IDEA Bazel plugin](https://plugins.jetbrains.com/plugin/8609-bazel)

# Usage
## Clone the repository:
```
git clone https://github.com/lolski/project-template-java.git
cd project-template-java
```

## Building
Building and running the template:
```
bazel build //:distribution
cd bazel-genfiles/
unzip project-template-java.zip
cd project-template-java/
./run
```

## Testing
Running tests locally:
```
bazel test //...
```

Running tests on RBE:
```
bazel test //... --config=rbe
```

## Adding Maven Dependencies
1. Add the Maven coordinate into `dependencies/maven/dependencies.yaml`
2. Run `./dependencies/maven/update.sh`. If successful, you can expect output that looks like this:
```
./dependencies/maven/update.sh 
INFO: Invocation ID: fce83792-4f71-4f72-b24f-64bf9aa4e4fe
INFO: Build options have changed, discarding analysis cache.
INFO: Analysed target //dependencies/tools:bazel-deps (16 packages loaded, 463 targets configured).
INFO: Found 1 target...
Target //dependencies/tools:bazel-deps up-to-date:
  bazel-bin/dependencies/tools/bazel-deps.jar
  bazel-bin/dependencies/tools/bazel-deps
INFO: Elapsed time: 0.326s, Critical Path: 0.00s
INFO: 0 processes.
INFO: Build completed successfully, 1 total action
INFO: Build completed successfully, 1 total action
wrote 5 targets in 5 BUILD files
```

# Troubleshooting
**Error about "bazel does not currently work properly from paths containing spaces":**
```
bazel build //:distribution
ERROR: bazel does not currently work properly from paths containing spaces (com.google.devtools.build.lib.runtime.BlazeWorkspace@4a053256).
```

Do a `pwd` and make sure your project isn't in a path which contains whitespace like this:
```
$ pwd
/home/martin/bazel project
```

**Error about "Unrecognized VM option" on a Mac machine**:
```
$ bazel build //:distribution
INFO: Invocation ID: 0819e626-a3ea-41ed-9bf8-0fa9040780f9
INFO: Analysed target //:distribution (0 packages loaded, 0 targets configured).
INFO: Found 1 target...
ERROR: /Users/syedirtazaraza/Desktop/PRECISION_MEDICINE/project-template-java/common/BUILD:1:1: Building common/libcommon.jar (1 source file) failed: Worker process did not return a WorkResponse:

---8<---8<--- Start of log, file at /private/var/tmp/_bazel_syedirtazaraza/f04d244fc24d715a834a0afe6107176d/bazel-workers/worker-2-Javac.log ---8<---8<---
Unrecognized VM option 'CompactStrings'
Error: Could not create the Java Virtual Machine.
Error: A fatal exception has occurred. Program will exit.
---8<---8<--- End of log ---8<---8<---
Target //:distribution failed to build
Use --verbose_failures to see the command lines of failed build steps.
INFO: Elapsed time: 0.191s, Critical Path: 0.05s
INFO: 0 processes.
FAILED: Build did NOT complete successfully
```

  1. Make sure to update to the latest Java
  2. If `bazel version` returns something like `0.20.0-homebrew` rather than `0.20.0`, you're not using the right Bazel. Make Make sure you install the one from `bazelbuild/tap`. : https://docs.bazel.build/versions/master/install-os-x.html
