#!/usr/bin/env python

import os
import subprocess as sp
import time
from grakn.client import GraknClient


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

    external_ip = sp.check_output(['gcloud', 'compute', 'instances', 'describe', instance, '--format=get(networkInterfaces[0].accessConfigs[0].natIP)', '--zone', 'europe-west1-b'])[:-1]

    uri = external_ip + ':48555'

    client = None
    while client is None:
        try:
            client = GraknClient(uri=uri)
        except Exception:
            time.sleep(120)

    with client.session(keyspace="grakn") as session:
        ## creating a read transaction
        with session.transaction().read() as read_transaction:
            answer_iterator = read_transaction.query("match $x isa thing; get;")

    client.close()


finally:
    lprint('Deleting the BioGrakn instance')
    gcloud_instances_delete(instance)