#!/bin/bash

set -ex

BIOGRAKN_DISTRIBUTION="biograkn-linux"

#add-apt-repository -y ppa:openjdk-r/ppa
#apt-get update
#apt-get upgrade -y
#apt-get install -y openjdk-8-jdk python python-pip unzip
#pip install jinja2-cli pyyaml
#
#adduser --disabled-password --gecos "" grakn

#mkdir /var/log/grakn && \
#    chown grakn:grakn /var/log/grakn && \
#    chmod 755 /var/log/grakn

tar -xf /tmp/deployment/"$BIOGRAKN_DISTRIBUTION".tar.gz -C /tmp/deployment && \
    cp -r /tmp/deployment/dist/"$BIOGRAKN_DISTRIBUTION" /opt/biograkn

#
#     && \
#    jinja2 /tmp/deployment/grakn.properties.j2 /tmp/deployment/grakn-properties-and-service-config.yml > /tmp/deployment/grakn.properties && \
#    jinja2 /tmp/deployment/grakn.service.j2  /tmp/deployment/grakn-properties-and-service-config.yml > /tmp/deployment/grakn.service && \
#    cp -f /tmp/deployment/grakn.properties /opt/grakn/conf/grakn.properties && \
#    cp -f /tmp/deployment/cassandra-env.sh /opt/grakn/server/services/cassandra/cassandra-env.sh && \
#    chown -R grakn:grakn /opt/grakn && \
#    chmod 755 /opt/grakn

mkdir /opt/c2d && \
    cp /tmp/deployment/startup-script.sh /opt/c2d/ && \
    chmod +x /opt/c2d/startup-script.sh && \
    cp /tmp/deployment/shutdown-script.sh /opt/c2d/ && \
    chmod +x /opt/c2d/shutdown-script.sh && \
    chown root:root /opt/c2d/

#cp /tmp/deployment/grakn.service /lib/systemd/system/grakn.service && \
#    chown grakn:grakn /lib/systemd/system/grakn.service


#cp /tmp/deployment/default-grakn-environmentfile /etc/default/grakn && \
#    chown grakn:grakn /etc/default/grakn
#
#systemctl daemon-reload
#systemctl disable grakn
#
#wget --no-verbose http://archive.apache.org/dist/cassandra/3.11.3/apache-cassandra-3.11.3-bin.tar.gz \
#    -O /opt/apache-cassandra-3.11.3-bin.tar.gz
#
#ln -s /opt/grakn/grakn /usr/local/bin/grakn
#ln -s /opt/grakn/graql /usr/local/bin/graql
#
#mkdir /tmp/deployment/oss-compliance && \
#    unzip /tmp/deployment/oss-licenses.zip -d /tmp/deployment/oss-compliance && \
#    unzip /tmp/deployment/oss-src.zip -d /tmp/deployment/oss-compliance && \
#    chown -R root:root /tmp/deployment/oss-compliance && \
#    cp -r /tmp/deployment/oss-compliance /etc/oss-compliance