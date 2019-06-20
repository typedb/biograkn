#!/usr/bin/env python

import subprocess as sp
import shutil
import os
from pwd import getpwnam

sp.check_call(['curl', '-L', 'https://releases.hashicorp.com/packer/1.4.0/packer_1.4.0_linux_amd64.zip', '-o', 'packer.zip'])

sp.check_call(['unzip', 'packer.zip'])

shutil.copy('./install.sh', './dist')
shutil.copy('./shutdown-script.sh', './dist')
shutil.copy('./startup-script.sh', './dist')


def filecopy(src, dest, owner=None, mode=None):
    shutil.copy(src, dest)
    if owner:
        owner_pwnam = getpwnam(owner)
        os.chown(dest, owner_pwnam.pw_uid, owner_pwnam.pw_gid)
    if mode:
        os.chmod(dest, mode)

filecopy(
    os.path.join('/tmp/deployment/', 'rc.local'),
    '/etc/rc.local',
    'root',
    0o755
)

sp.check_call([
    './packer',
    'build',
    'packer-config-snapshot.json'
], env=os.environ)
