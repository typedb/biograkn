#!/usr/bin/env python

import os
import subprocess as sp
import sys
import tempfile
import shutil


credential = os.getenv('GCP_CREDENTIAL')
project = 'grakn-dev'

credential_file = '/tmp/gcp-credential.json'
with open(credential_file, 'w') as f:
    f.write(credential)

sp.check_call(['gcloud', 'auth', 'activate-service-account', '--key-file', credential_file])
sp.check_call(['gcloud', 'config', 'set', 'project', project])

# if this file is not removed gsutil throws an error and wont run
sp.check_call(['sudo', 'rm', '-f', '/etc/boto.cfg'])

sp.check_call(['gsutil', 'defacl', 'ch', '-u', 'AllUsers:READER', 'gs://biograkn'])
sp.check_call(['gsutil', 'rsync', '-R', './dist/', 'gs://biograkn'])


sp.check_call(['curl', '-L', 'https://github.com/tcnksm/ghr/releases/download/v0.10.2/ghr_v0.10.2_linux_386.tar.gz', '-o', 'ghr_v0.10.2_linux_386.tar.gz'])

sp.check_call(['tar', '-xvzf', 'ghr_v0.10.2_linux_386.tar.gz'])

target_commit_id = sys.argv[1]

with open('VERSION') as version_file:
    github_tag = version_file.read().strip()

directory_to_upload = tempfile.mkdtemp()

github_token = os.getenv('DEPLOY_GITHUB_TOKEN')

try:
    exit_code = sp.call([
        'ghr_v0.10.2_linux_386/ghr',
        '-u', 'graknlabs',
        '-r', 'biograkn',
        '-b', 'hello world',
        '-c', target_commit_id,
        '-delete', '-draft', github_tag,  # TODO: tag must reference the current commit
        directory_to_upload
    ], env={'GITHUB_TOKEN': github_token})
finally:
    shutil.rmtree(directory_to_upload)
