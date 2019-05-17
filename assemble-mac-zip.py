import subprocess as sp
import sys

_, grakn, migrator, out = sys.argv

sp.check_call(["unzip", grakn])

sp.check_call(["nohup", "grakn-core-all-mac/grakn", "server", "start"])

migrator = migrator.split(" ")

# sp.check_call([migrator[0]])

sp.check_call(["grakn-core-all-mac/grakn", "server", "stop"])

sp.check_call(["mv", "grakn-core-all-mac", "biograkn-mac"])

sp.check_call(["zip", "-r", out, "biograkn-mac"])

