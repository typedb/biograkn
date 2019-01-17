java_library(
    name = "common",
    srcs = glob(["*.java"]),
    visibility = [ "//visibility:public" ]
)

java_test(
    name = "date-util-test",
    test_class = "grakn.template.java.DateUtilTest",
    srcs = [ "test/DateUtilTest.java"],
    deps = [ "common" ]
)