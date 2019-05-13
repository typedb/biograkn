#!/bin/bash
LOG_HOME=/var/log/grakn/
DB_HOME=/mnt/data1/

CLUSTER_LOG=${LOG_HOME}cluster.log

NODETOOL="/opt/grakn/server/services/cassandra/nodetool"

${NODETOOL} drain >> ${CLUSTER_LOG}

systemctl disable grakn
systemctl stop grakn

rm ${DB_HOME}cassandra/commitlog/*
rm ${DB_HOME}cassandra/saved_caches/*

