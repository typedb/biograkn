import subprocess as sp
import sys

_, grakn, migrator, out = sys.argv

sp.check_call(["tar", "-xvzf", grakn])

sp.check_call(["nohup", "grakn-core-all-linux/grakn", "server", "start"])

migrator = migrator.split(" ")

# sp.check_call([migrator[0]])

sp.check_call(["grakn-core-all-linux/grakn", "server", "stop"])

sp.check_call(["mv", "grakn-core-all-linux", "biograkn-linux"])

sp.check_call(["tar", "-czf", out, "biograkn-linux"])

