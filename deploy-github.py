import os
import subprocess as sp

credential = os.getenv('GCP_CREDENTIAL')
project = 'grakn-dev'

credential_file = '/tmp/gcp-credential.json'
with open(credential_file, 'w') as f:
    f.write(credential)

sp.check_call(['gcloud', 'auth', 'activate-service-account', '--key-file', credential_file])
sp.check_call(['gcloud', 'config', 'set', 'project', project])

sp.check_call(['sudo', 'rm', '-f', '/etc/boto.cfg'])

sp.check_call(['gsutil', 'defacl', 'ch', '-u', 'AllUsers:READER', 'gs://biograkn'])
sp.check_call(['gsutil', 'rsync', '-R', './biograkn/dist/', 'gs://biograkn'])

