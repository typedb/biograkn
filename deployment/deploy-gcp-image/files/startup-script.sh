#!/bin/bash

SELF_DNS=$(curl -s --header "Metadata-Flavor: Google" http://metadata.google.internal/computeMetadata/v1/instance/hostname | cut -d . -f 1)
BOOTSTRAPNODE=$(curl -s --header "Metadata-Flavor: Google" http://metadata.google.internal/0.1/meta-data/attributes/BOOTSTRAPNODE)
EXTRA_SEED_NODE=$(curl -s --header "Metadata-Flavor: Google" http://metadata.google.internal/0.1/meta-data/attributes/EXTRA_SEED_NODE)
NO_OF_NODES=$(curl -s --header "Metadata-Flavor: Google" http://metadata.google.internal/0.1/meta-data/attributes/NO_OF_NODES)
STORAGE_USER_PASSWORD=$(curl -H "Metadata-Flavor: Google" http://metadata.google.internal/computeMetadata/v1/instance/attributes/STORAGE_USER_PWD)
STORAGE_SYSTEM_PASSWORD=$(curl -H "Metadata-Flavor: Google" http://metadata.google.internal/computeMetadata/v1/instance/attributes/STORAGE_SYSTEM_PWD)
EXTRA_SEED_LIMIT=5
TRUE=0

log() {
  CURRENT_DATE_TIME=`date "+%Y-%m-%d %H:%M:%S"`
  echo "${CURRENT_DATE_TIME} [cluster] $1" >> $CLUSTER_LOG
}

log_command() {
  local COMMAND=$1
  log "Executing command: ${COMMAND}"
  local LOGGED_COMMAND="${COMMAND} >> ${CLUSTER_LOG} 2>&1"
  eval "${LOGGED_COMMAND}"
}

format_disk() {
  mkfs.ext4 -m 0 -F -E lazy_itable_init=0,lazy_journal_init=0,discard /dev/disk/by-id/google-${HOSTNAME}-data
}

mount_disk() {
  
  if [ $DEPLOYMENT_INITIALISED -ne $TRUE ]; then
    mkdir /mnt
    format_disk
  fi

  mount -o discard,defaults /dev/disk/by-id/google-${HOSTNAME}-data /mnt

  mkdir -p ${DB_HOME}
  mkdir -p ${DB_HOME}cassandra/data
  mkdir -p ${DB_HOME}cassandra/saved_caches
  mkdir -p ${DB_HOME}cassandra/commitlog
  mkdir -p ${DB_HOME}queue/
  chmod -R 777 ${DB_HOME}

  log "Node ${SELF_DNS} finished the disk mount procedure."
}

get_vm_id() {
  VM_ID=$SELF_DNS
  while [[ $VM_ID =~ (.*)-[0-9]+(-vm) ]]; do
    VM_ID=${VM_ID#*-}
  done
  VM_ID=$(echo $VM_ID | tr -dc '0-9')
}

start_storage() {
  su -c "grakn server start storage" grakn
}

stop_storage() {
  su -c "grakn server stop storage" grakn
}

set_paths() {
  GRAKN_HOME=/opt/grakn/
  LOG_HOME=/var/log/grakn/
  DB_HOME=/mnt/data1/

  STORAGE_CONFIG=${GRAKN_HOME}/server/services/cassandra/cassandra.yaml
  STORAGE_ENV_FILE=${GRAKN_HOME}/server/services/cassandra/cassandra-env.sh
  GRAKN_CONFIG=${GRAKN_HOME}/conf/grakn.properties
  CLUSTER_LOG=${LOG_HOME}cluster.log

  CASS_DIST=apache-cassandra-3.11.3
  CASS_PACKAGE=${CASS_DIST}-bin.tar.gz
  CASS_HOME=/opt/${CASS_DIST}/
  CASS_PACKAGE_PATH=/opt/${CASS_PACKAGE}
  CQLSH=${CASS_HOME}bin/cqlsh
}

get_variables() {

  set_paths

  get_vm_id

  if [[ -z "$BOOTSTRAPNODE" ]]; then
    BOOTSTRAPNODE="${SELF_DNS}"
  fi
  if [[ -z "$EXTRA_SEED_NODE" ]]; then
    EXTRA_SEED_NODE="${SELF_DNS}"
  fi

  [ ! -f ${CASS_PACKAGE_PATH} ]; DEPLOYMENT_INITIALISED=$?

  if [ $DEPLOYMENT_INITIALISED -eq $TRUE ]; then
    log "Deployment already initialised."
  fi

  [ $NO_OF_NODES -ge $EXTRA_SEED_LIMIT ]; EXTRA_SEED_NEEDED=$?
  [ "$SELF_DNS" == "$BOOTSTRAPNODE" ] || ( [ "$SELF_DNS" == "$EXTRA_SEED_NODE" ] && [ $EXTRA_SEED_NEEDED -eq $TRUE ] ); IS_SEED_NODE=$?

  if [ $EXTRA_SEED_NEEDED -eq $TRUE ]; then
    STORAGE_SEEDS="${BOOTSTRAPNODE}, ${EXTRA_SEED_NODE}"
  else
    STORAGE_SEEDS="${BOOTSTRAPNODE}"
  fi

  # set replication-factor according to the number of nodes
  RF=${NO_OF_NODES}
  log "Number of nodes in this cluster: ${NO_OF_NODES} node(s)."
  if [ "${RF}" -gt 3 ]; then
    RF=3
  fi
}

init_node() {

  get_variables

  mount_disk

  if [ $IS_SEED_NODE -eq $TRUE ]; then
    log "Node ${SELF_DNS} is a seed node."
  fi

}

define_users(){
  cd /opt/

  tar -xzf ${CASS_PACKAGE}

  cd "${GRAKN_HOME}"

  start_storage
  sleep 15
  log_command "./grakn server status"

  ${CQLSH} ${SELF_DNS} -u cassandra -p cassandra -e "CREATE USER grakn WITH PASSWORD '${STORAGE_USER_PASSWORD}' SUPERUSER;"
  log "Storage user password was reset."

  ${CQLSH} ${SELF_DNS} -u grakn -p ${STORAGE_USER_PASSWORD} -e "CREATE USER graknsystemuser WITH PASSWORD '${STORAGE_SYSTEM_PASSWORD}' SUPERUSER;"
  log "Storage system password was reset."

  ${CQLSH} ${SELF_DNS} -u grakn -p ${STORAGE_USER_PASSWORD} -e "DROP USER cassandra;"

  stop_storage
  log_command "./grakn server status"
}

configure_authentication(){

  if [ "$SELF_DNS" == "$BOOTSTRAPNODE" ]; then
    define_users
  fi

  sed -ri 's/^graknsystem.username=.*/graknsystem.username=graknsystemuser/' "${GRAKN_CONFIG}"
  sed -ri 's/^graknsystem.password=.*/graknsystem.password='"${STORAGE_SYSTEM_PASSWORD}"'/' "${GRAKN_CONFIG}"
  log "Grakn system credentials were updated."

  rm -rf ${CASS_HOME}
  rm ${CASS_PACKAGE_PATH}
}

configure_storage() {
  : ${STORAGE_LISTEN_ADDRESS='auto'}
  if [ "$STORAGE_LISTEN_ADDRESS" = 'auto' ]; then
    STORAGE_LISTEN_ADDRESS="${SELF_DNS}"
  fi

  : ${STORAGE_BROADCAST_ADDRESS="$STORAGE_LISTEN_ADDRESS"}
  : ${STORAGE_RPC_ADDRESS="$STORAGE_LISTEN_ADDRESS"}

  if [ "$STORAGE_BROADCAST_ADDRESS" = 'auto' ]; then
    STORAGE_BROADCAST_ADDRESS="${SELF_DNS}"
  fi

  : ${STORAGE_BROADCAST_RPC_ADDRESS:=$STORAGE_BROADCAST_ADDRESS}
  
  sed -ri 's/(- seeds:).*/\1 "'"$STORAGE_SEEDS"'"/' "${STORAGE_CONFIG}"

  for yaml in \
    broadcast_address \
    broadcast_rpc_address \
    cluster_name \
    endpoint_snitch \
    listen_address \
    num_tokens \
    rpc_address \
    start_rpc \
  ; do
    var="STORAGE_${yaml^^}"
    val="${!var}"
    if [ "$val" ]; then
      sed -ri 's/^(# )?('"$yaml"':).*/\2 '"$val"'/' "${STORAGE_CONFIG}"
    fi
  done

  #make nodetool connections possible within the cluster
  sed -ri 's/^LOCAL_JMX=.*/LOCAL_JMX=no/' "${STORAGE_ENV_FILE}"
  sed -i -e 's/jmxremote.authenticate=true/jmxremote.authenticate=false/g' "${STORAGE_ENV_FILE}"

  # Fix connections strings in grakn.properties
  sed -ri 's/^storage.hostname=.*/storage.hostname='"${STORAGE_LISTEN_ADDRESS}"'/' "${GRAKN_CONFIG}"
  sed -ri 's/^queue.host=.*/queue.host='"${STORAGE_LISTEN_ADDRESS}"':6379/' "${GRAKN_CONFIG}"

  log "Setting replication-factor to ${RF}"
  sed -ri 's/^storage.cassandra.replication-factor=.*/storage.cassandra.replication-factor='"${RF}"'/' "${GRAKN_CONFIG}"
}

configure() {

  cd "${GRAKN_HOME}"

  configure_storage

  configure_authentication

  log "Node ${SELF_DNS} finished configuring."
}

wait_to_join() {
  #Allow two minutes between storage node startups.
  local INITIAL_SLEEP=60
  local SLEEP_QUANT=120

  sleep $INITIAL_SLEEP

  get_no_of_storage_nodes_up
  log "${NODES_UP} storage nodes are already up."

  local ID_SHIFT=$(( $NODES_UP + 1 ))
  local SLEEP_TIME=$(( $SLEEP_QUANT * ($VM_ID - $ID_SHIFT) ))

  log "Node ${SELF_DNS} will now wait ${SLEEP_TIME} seconds to join the cluster."
  sleep $SLEEP_TIME
}

get_no_of_storage_nodes_up(){
  local WC_OUTPUT=`grakn cluster status | grep UN | wc -l`
  NODES_UP=$((WC_OUTPUT))
}

wait_for_all_nodes_to_join(){
  get_no_of_storage_nodes_up
  while [ "$NODES_UP" -ne "$NO_OF_NODES" ]; do
    log "${NODES_UP}/${NO_OF_NODES} of storage nodes up. Waiting for others to join."
    sleep 20
    get_no_of_storage_nodes_up
  done
}

start_storage_cluster() {

  if [ ! $IS_SEED_NODE -eq $TRUE ]; then
    wait_to_join
  fi

  log "Node ${SELF_DNS} is ready to join the storage cluster."

  start_storage
  set_log_permissions
  log "Node ${SELF_DNS} joined the storage cluster."

  wait_for_all_nodes_to_join
  log "All storage nodes are up"
}

set_log_permissions() {
  chmod -R 777 ${LOG_HOME}
  chown -R grakn:grakn ${LOG_HOME}
}

init_node

if [ $DEPLOYMENT_INITIALISED -ne $TRUE ]; then
  configure
fi

start_storage_cluster

if [ "$SELF_DNS" != "$BOOTSTRAPNODE" ]; then
  log "Waiting for 60 seconds for the bootstrap node to finish initialising the system."
  sleep 60 
fi
systemctl enable grakn
log "Node ${SELF_DNS} enabled grakn service."

systemctl start grakn
log "Node ${SELF_DNS} started grakn service."

set_log_permissions

log "Deployment startup finished."
