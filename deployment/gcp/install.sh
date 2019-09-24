#!/bin/bash

set -ex

BIOGRAKN_DISTRIBUTION="biograkn-linux"

tar -xf /tmp/deployment/"$BIOGRAKN_DISTRIBUTION".tar.gz -C /opt/biograkn --strip-components=1

chmod -R a+rwX /opt/biograkn/

apt-get install -y openjdk-8-jdk

mkdir /opt/c2d
cp /tmp/deployment/startup-script.sh /opt/c2d/
chmod +x /opt/c2d/startup-script.sh
cp /tmp/deployment/shutdown-script.sh /opt/c2d/
chmod +x /opt/c2d/shutdown-script.sh
chown root:root /opt/c2d/

cp /tmp/deployment/rc.local /etc/rc.local
chmod +x /etc/rc.local
chown root:root /etc/rc.local

