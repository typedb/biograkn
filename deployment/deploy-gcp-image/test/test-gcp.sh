#!/bin/bash

set -x

STACK_NAME=$1
GC_GENERATOR_HOST=$2
GC_GENERATOR_PRIVATE_IP=$3
GENERATOR_CIDR="$GC_GENERATOR_PRIVATE_IP/32"
CONTAINER_HOST=$(curl ipinfo.io/ip)
CONTAINER_CIDR="$CONTAINER_HOST/32"
NUM_INSTANCES=200

get_stack_info(){
  INSTANCES=$(gcloud deployment-manager deployments describe --project grakn-dev $STACK_NAME | grep instance | sed 's|\('"$STACK_NAME"'-[0-9]*-vm\).*|\1|' )
  NO_OF_NODES=$( echo "$INSTANCES" | wc -l )
  NODE_DNS=$STACK_NAME"-1-vm"

  ssh_command $NODE_DNS

  #dummy ssh command to propagate ssh keys 
  ${SSH_COMMAND} "grakn server status" > dummy_ssh

  CURL_COMMAND="curl -s -H \"Metadata-Flavor: Google\" http://metadata.google.internal/computeMetadata/v1/instance/attributes/STORAGE_USER_PWD"
  ${SSH_COMMAND} "${CURL_COMMAND}" > output

  GRAKN_USER_PASSWORD=$( cat output )
  echo $NO_OF_NODES
  echo $NODE_DNS
}

ssh_command(){
  SSH_COMMAND="gcloud compute ssh --project grakn-dev --zone us-central1-f ubuntu@$1 --command"
}

stack_status(){
  local STACK_STATUS=$(gcloud deployment-manager deployments list --project grakn-dev | grep $STACK_NAME)
  echo "${STACK_STATUS}"
  STACK_UP=$(echo "$STACK_STATUS" | grep DONE | wc -l)
}

add_firewall_rules(){
  local SOURCE_CIDR=$1
  local GENERATOR_CIDR=$2
 
  ##add ingress rule to allow traffic from generator host to the stack
  gcloud compute firewall-rules create "generator-grpc-rule" --allow tcp:48555 \
--source-ranges="${GENERATOR_CIDR}" \
--direction INGRESS \
--project grakn-dev

  ##add ingress rule to allow ssh to generator from container
  gcloud compute firewall-rules create "ssh-container-rule" --allow tcp:22 \
--source-ranges="${SOURCE_CIDR}" \
--direction INGRESS \
--project grakn-dev
}

verify_licenses(){
  local HOST=$1
  ssh_command $HOST

  ${SSH_COMMAND} "curl -s -H \"Metadata-Flavor: Google\" http://metadata.google.internal/computeMetadata/v1/instance/licenses/?recursive=true" > licenses
  local LICENSES=$( cat licenses )
  echo "$LICENSES"

  local NO_OF_LICENSES=$( echo "$LICENSES" | grep -oh id | wc -w)

  if [ "$NO_OF_LICENSES" -ne 2 ]; then
    echo "Missing licenses (found: $NO_OF_LICENSES). Quitting..."
    exit 1
  fi

  local GRAKN_LICENSE="8083965804406746542"

  local LICENSE_=$( echo "$LICENSES" | grep -oh $GRAKN_LICENSE | wc -w)

  if [ "$LICENSE_" -ne 1 ]; then
    echo "Grakn License id: $GRAKN_LICENSE missing. Quitting..."
    exit 1
  fi

}

source ./cluster-util.sh

gcloud deployment-manager deployments create ${STACK_NAME} --config ../deploy/test_config.yaml --project grakn-dev


wait_for_stack

add_firewall_rules $CONTAINER_CIDR $GENERATOR_CIDR

#sleep 60 # TODO: consider removing if not needed

get_stack_info

wait_for_storage $NODE_DNS

wait_for_engine $NODE_DNS

sleep 90

test_grpc $NODE_DNS $GRAKN_USER_PASSWORD

test_data_load $NODE_DNS $GRAKN_USER_PASSWORD $GC_GENERATOR_HOST

verify_licenses $NODE_DNS
