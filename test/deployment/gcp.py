#!/usr/bin/env python

import collections
import glob
import json
import logging
import os
import re
from googleapiclient.discovery import build
from google.oauth2 import service_account
import paramiko
import platform
import subprocess as sp
import tempfile
import time

GCPInstance = collections.namedtuple('GCPInstance',
                                     ['name', 'zone', 'public_ip', 'password'])

logger = logging.getLogger(__name__)
logging.basicConfig(
    format='[%(asctime)s.%(msecs)03d]: %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
)
logger.level = logging.DEBUG

TIMEOUT = 60 * 20  # 20 minutes

ENV_VARIABLES = ['GCP_DEPLOY_KEY', 'AWS_SSH_KEY_PAIR']


def verify_environment():
    logger.debug('Verifying environment variables to be present: %s',
                 ENV_VARIABLES)
    for var in ENV_VARIABLES:
        if not os.getenv(var):
            raise ValueError('Should specify {} env variable'.format(var))


def write_ssh_key():
    key_file = tempfile.NamedTemporaryFile(delete=False)
    with open(key_file.name, 'w') as kf:
        key = os.getenv('AWS_SSH_KEY_PAIR')
        # private key string stored in env variable is not
        # parsed correctly by paramiko
        # specifically, headers should be on separate lines
        key = key.replace('-----BEGIN RSA PRIVATE KEY----- ', '')
        key = key.replace(' -----END RSA PRIVATE KEY-----', '')
        kf.writelines([
            '-----BEGIN RSA PRIVATE KEY-----\n',
            key + '\n',
            '-----END RSA PRIVATE KEY-----\n'
        ])
    return key_file.name


# TODO(vmax): now script depends on invocation folder
class GCPClient:
    def __init__(self, gcp_credential, key_file):
        credential = service_account.Credentials.from_service_account_info(
            json.loads(gcp_credential),
            scopes=['https://www.googleapis.com/auth/cloud-platform'])

        self._deploymentmanager = build('deploymentmanager',
                                        'v2',
                                        credentials=credential,
                                        cache_discovery=False)
        self._compute = build('compute', 'v1', credentials=credential, cache_discovery=False)
        self._key = paramiko.RSAKey.from_private_key_file(key_file)
        self._ssh_client_cache = {}

    def _read_imports(self):
        files = {
            os.path.basename(x): open(x).read()
            for x in glob.glob('./deployment/deploy-gcp-image/deploy/**/*.*') +
                     glob.glob('./deployment/deploy-gcp-image/deploy/grakn.jinja*')
        }

        files['grakn.jinja'] = files['grakn.jinja'].replace(
            # embed SSH public key
            '# {SSH_KEYS_PLACEHOLDER}',
            '        - key: ssh-keys\n          value: "{}"'.format(self._ssh_public_key())
        ).replace(
            # embed source image name
            '# {SOURCE_IMAGE_PLACEHOLDER}',
            self._get_image_name()
        ).replace(
            # allow inbound access on Grakn port
            'properties["enableTcp48555"]',
            'true'
        )

        return [{'name': k, 'content': v} for k, v in files.items()]

    def _ssh_public_key(self):
        return 'circleci:ssh-rsa {pubkey} circleci'.format(
            pubkey=self._key.get_base64())

    def _get_image_name(self):
        commit = sp.check_output(['git', 'rev-parse', 'HEAD']).strip()
        return 'grakn-kgms-snapshot-{}'.format(commit)

    def _wait_for_operation(self, operation):
        while True:
            result = self._deploymentmanager.operations().get(
                project='grakn-dev', operation=operation).execute()

            if result['status'] == 'DONE':
                logger.debug("[operation '%s']: done", operation)
                if 'error' in result:
                    logger.error("[operation '%s']: error", operation)
                    raise Exception(result['error'])
                return result

            time.sleep(1)

    def _get_public_ip(self, name, zone):
        response = self._compute.instances().list(
            project='grakn-dev', zone=zone,
            filter='name={}'.format(name)).execute()
        if len(response['items']) == 1:
            return response['items'][0]['networkInterfaces'][0]['accessConfigs'][0]['natIP']

    def ssh_session(self, instance):
        import paramiko
        if instance in self._ssh_client_cache:
            return self._ssh_client_cache[instance]

        client = paramiko.client.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        client.connect(instance.public_ip, username='circleci', pkey=self._key)

        self._ssh_client_cache[instance] = client
        return client

    def is_node_ready(self, instance):
        client = self.ssh_session(instance)
        _, stdout, _ = client.exec_command(
            "grep --quiet 'Deployment startup finished.' /var/log/grakn/cluster.log"
        )
        while not stdout.channel.exit_status_ready():
            # wait until exit code is ready
            pass

        return stdout.channel.exit_status == 0

    def create_deployment(self, name):
        config = {
            'name': name,
            'target': {
                'config': {
                    'content':
                        open(
                            './deployment/deploy-gcp-image/deploy/test_config.yaml'
                        ).read()
                },
                'imports': self._read_imports()
            },
        }
        operation = self._deploymentmanager.deployments().insert(
            body=config, project='grakn-dev').execute()
        self._wait_for_operation(operation['name'])

    def delete_deployment(self, name):
        operation = self._deploymentmanager.deployments().delete(
            project='grakn-dev', deployment=name).execute()
        self._wait_for_operation(operation['name'])

    def instances_in_deployment(self, name):
        import yaml
        instances = []
        zone_regex = re.compile('.*zones/(?P<zone>[^/]*)/.*')
        response = self._deploymentmanager.resources().list(
            project='grakn-dev', deployment=name).execute()
        for i in response['resources']:
            if i['type'] == 'compute.v1.instance':
                name = i['name']
                zone = re.search(zone_regex, i['url']).group('zone')
                password = None
                metadata = yaml.safe_load(i['finalProperties'])
                for kv in metadata['metadata']['items']:
                    if kv['key'] == 'STORAGE_USER_PWD':
                        password = kv['value']
                instances.append(
                    GCPInstance(name, zone, self._get_public_ip(name, zone),
                                password))
        return instances


if __name__ == "__main__":
    client = None
    try:
        verify_environment()

        logger.debug('Creating Google Compute Platform client')
        client = GCPClient(os.getenv('GCP_DEPLOY_KEY'), write_ssh_key())

        logger.debug('Creating deployment')
        client.create_deployment('test-grakn-kgms')

        instances = client.instances_in_deployment('test-grakn-kgms')
        logger.debug('Instances: %s', instances)

        logger.debug('Waiting until all nodes are up...')
        start_time = time.time()

        all_nodes = set(instances)
        ready_nodes = set()

        while len(ready_nodes) < len(
                all_nodes) and time.time() - start_time < TIMEOUT:
            for node in all_nodes - ready_nodes:
                if client.is_node_ready(node):
                    ready_nodes.add(node)
                    logger.debug('[%d/%d] Node with name %s is up',
                                 len(ready_nodes), len(all_nodes), node.name)
                time.sleep(15)

        if ready_nodes != all_nodes:
            raise Exception(
                'Not all nodes came up on time, expected {}, got {}'.format(
                    all_nodes, ready_nodes))

        num_instances = len(instances)
        for i, instance in enumerate(instances, start=1):
            logger.debug('Testing instance %d/%d', i, num_instances)
            logger.debug('Instance public IP: %s', instance.public_ip)

            env = dict(os.environ)
            env.update({
                'KGMS_APPLICATION_TEST_ADDRESS': '{}:48555'.format(instance.public_ip),
                'KGMS_APPLICATION_TEST_USERNAME': 'grakn',
                'KGMS_APPLICATION_TEST_PASSWORD': instance.password,
            })

            # logger.debug(
            #     'Running bazel run //test/common:kgms-application-test')
            # start_time = time.time()
            # sp.check_call(
            #     [
            #         'bazel',
            #         'run',
            #         '//test/common:kgms-application-test',
            #         '--config=rbe' if platform.system() == 'Linux' else '',
            #     ],
            #     env=env)
            # logger.debug('Took %0.2f seconds', time.time() - start_time)
    finally:
        logger.debug('Removing deployment')
        if client:
            client.delete_deployment('test-grakn-kgms')