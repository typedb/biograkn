#!/usr/bin/env python

import subprocess as sp
import shutil
import os

sp.check_call(['curl', '-L', 'https://releases.hashicorp.com/packer/1.4.0/packer_1.4.0_linux_amd64.zip', '-o', 'packer.zip'])

sp.check_call(['unzip', 'packer.zip'])

shutil.copy('/deployment/gcp/install.sh', './dist')
shutil.copy('/deployment/gcp/shutdown-script.sh', './dist')
shutil.copy('/deployment/gcp/startup-script.sh', './dist')

sp.check_call([
    './packer',
    'build',
    '/deployment/gcp/packer-config.json'
], env=os.environ)
