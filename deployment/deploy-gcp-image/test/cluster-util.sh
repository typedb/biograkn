#!/bin/bash

# requires the following definitions:
# - stack_status
# - ssh_command
# - add_firewall_rules

log() {
  CURRENT_DATE_TIME=`date "+%Y-%m-%d %H:%M:%S"`
  echo "${CURRENT_DATE_TIME} [cluster-ci] $1"
}

exit_on_failure(){
  rc=$?
  if [[ $rc != 0 ]]; then
    log "Exiting on command failure"
    exit $rc
  fi
}

node_ip(){
  ssh_command ubuntu $1
  ${SSH_COMMAND} "curl ipinfo.io/ip" > node_ip
  NODE_IP=$( cat node_ip )
}

ssh_status(){
  local HOST=$1
  local STATUS_COMMAND=$2
  local STATUS_STRING=$3

  ssh_command $HOST
  ${SSH_COMMAND} "${STATUS_COMMAND}" > status_output
  local STATUS_OUTPUT=$( cat status_output )

  echo "$STATUS_OUTPUT"
  WC_STATUS_OUTPUT=$(echo "$STATUS_OUTPUT" | grep "$STATUS_STRING" | wc -l)
}

engine_status(){
  ssh_status $1 "grakn server status" "Engine: RUNNING"
  ENGINE_UP=$((WC_STATUS_OUTPUT))
}

storage_nodes_up(){
  ssh_status $1 "grakn cluster status" UN
  NODES_UP=$((WC_STATUS_OUTPUT))
}

wait_for_stack(){
  stack_status
  while [ "$STACK_UP" -ne 1 ]; do
    if [ "$STACK_ERROR" -eq 1 ]; then
      echo "Stack error during deployment. Quitting..."
      exit 1
    fi 

    echo "Stack deploying. Waiting for deployment to finish."
    sleep 10
    stack_status
  done
}

wait_for_storage(){
  storage_nodes_up $1
  while [ "$NODES_UP" -ne "$NO_OF_NODES" ]; do
    echo "${NODES_UP}/${NO_OF_NODES} of storage nodes up. Waiting for others to join."
    sleep 20
    storage_nodes_up $1
  done
}

wait_for_engine(){
  engine_status $1
  while [ "$ENGINE_UP" -ne 1 ]; do
    echo "Engine not started. Waiting for the engine to start."
    sleep 10
    engine_status $1
  done
}

verify_query(){
  local HOST=$1
  local USER_PWD=$2
  local GRAQL_QUERY=$3
  local ANSWER_INSTANCES=$4

  local CONSOLE_COMMAND="graql console -u grakn -p ${USER_PWD}"
  echo $CONSOLE_COMMAND
  ssh_command $HOST

  ${SSH_COMMAND} "${CONSOLE_COMMAND} -e '$GRAQL_QUERY'" > query_output
  exit_on_failure

  local GRAQL_OUTPUT=$( cat query_output )
  echo "$GRAQL_OUTPUT"
  local COUNT=$(( GRAQL_OUTPUT ))
  
  if [ "$COUNT" -ne "$ANSWER_INSTANCES" ]; then
    exit 1
  fi
  
  echo "Query: ${GRAQL_QUERY} - verified!"
}
  
test_grpc(){
  local GRAQL_QUERY='match $x; aggregate count;'
  local SCHEMA_INSTANCES=6
  verify_query $1 $2 "$GRAQL_QUERY" $SCHEMA_INSTANCES
}

load_data(){
  local HOST=$1
  local GENERATOR_HOST=$2

  local GEN_COMMAND="/home/ubuntu/synthetic-data-generator-1.0-SNAPSHOT/sdgen $HOST:48555 grakn $GRAKN_USER_PASSWORD grakn insert 1 $NUM_INSTANCES"
  echo "$GEN_COMMAND"
  ssh_command $GENERATOR_HOST
  ${SSH_COMMAND} "${GEN_COMMAND}" > data_load
  exit_on_failure
  log "Synthetic data loaded successfully."
}

verify_data(){
  local HOST=$1
  local USER_PWD=$2

  local GRAQL_QUERY='match $x isa person; aggregate count;'
  verify_query $HOST $USER_PWD "$GRAQL_QUERY" $NUM_INSTANCES

  local GRAQL_QUERY='match $x isa relationship; aggregate count;'
  verify_query $HOST $USER_PWD "$GRAQL_QUERY" $(( 2* $NUM_INSTANCES - 1))

  local GRAQL_QUERY='match $x isa name; aggregate count;'
  verify_query $HOST $USER_PWD "$GRAQL_QUERY" $NUM_INSTANCES
}

test_data_load(){
  local HOST=$1
  local USER_PWD=$2
  local GENERATOR_HOST=$3 
  local GENERATOR_CIDR=$4/32
  local SOURCE_CIDR=$5/32

  load_data $HOST $GENERATOR_HOST

  verify_data $HOST $USER_PWD
}