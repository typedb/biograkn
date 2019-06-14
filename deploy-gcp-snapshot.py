import subprocess as sp
import shutil
import os

sp.check_call(['curl', '-L', 'https://releases.hashicorp.com/packer/1.4.0/packer_1.4.0_darwin_amd64.zip', '-o', 'packer.zip'])

sp.check_call(['unzip', 'packer.zip'])

shutil.copy('./install.sh', './dist')
shutil.copy('./shutdown-script.sh', './dist')
shutil.copy('./startup-script.sh', './dist')

sp.check_call([
    './packer',
    'build',
    '-debug',
    'packer-config.json'
], env=os.environ)
