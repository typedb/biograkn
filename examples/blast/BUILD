exports_files(["requirements.txt"])

load("@io_bazel_rules_python//python:python.bzl", "py_test")
load("@blast_example_pip//:requirements.bzl",
       blast_example_requirement = "requirement")

py_test(
    name = "test",
    main = "test.py",
    srcs = [
        "test.py",
        "migrate.py",
        "blast.py",
        "queries.py"
    ],
    deps = [
        "@graknlabs_client_python//:client_python",
        blast_example_requirement("biopython")
    ],
    data = [
        "uniprot-asthma-proteins.fasta",
        "schema.gql",
        "blast-output.xml"
    ]
)