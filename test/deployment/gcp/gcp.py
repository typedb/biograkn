#!/usr/bin/env python

import os
import subprocess as sp


def lprint(msg):
    # TODO: replace with proper logging
    from datetime import datetime
    print('[{}]: {}'.format(datetime.now().isoformat(), msg))


def gcloud_instances_create(instance):
    sp.check_call([
        'gcloud',
        'compute',
        'instances',
        'create',
        instance,
        '--image',
        'grakn-biograkn-snapshot-test',
        '--machine-type',
        'n1-standard-4',
        '--zone=europe-west1-b',
        '--project=grakn-dev'
    ])

def gcloud_instances_delete(instance):
    sp.check_call([
        'gcloud',
        '--quiet',
        'compute',
        'instances',
        'delete',
        instance,
        '--delete-disks=all',
        '--zone=europe-west1-b'
    ])

# TODO: exit if CIRCLE_BUILD_NUM and $GCP_CREDENTIAL aren't present
credential = os.getenv('GCP_CREDENTIAL')
project = 'grakn-dev'
instance = 'circleci-' + os.getenv('CIRCLE_PROJECT_REPONAME') + '-' + os.getenv('CIRCLE_JOB') + '-' + os.getenv('CIRCLE_BUILD_NUM')

try:
    lprint('Configure the gcloud CLI')
    credential_file = '/tmp/gcp-credential.json'
    with open(credential_file, 'w') as f:
        f.write(credential)
    sp.check_call(['gcloud', 'auth', 'activate-service-account', '--key-file', credential_file])
    sp.check_call(['gcloud', 'config', 'set', 'project', project])
    sp.check_call(['ssh-keygen', '-t', 'rsa', '-b', '4096', '-N', '', '-f', os.path.expanduser('~/.ssh/google_compute_engine')])

    lprint('Creating a BioGrakn instance "' + instance + '"')
    gcloud_instances_create(instance)

finally:
    lprint('Deleting the BioGrakn instance')
    gcloud_instances_delete(instance)