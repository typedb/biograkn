#!/bin/bash

INSTANCE_NAME="assemble-biograkn-linux"
ZONE="us-east1-b"

mkdir ~/logs
LOG=~/logs/launch_executor_$INSTANCE_NAME.log


#export GCP_ACCOUNT_FILE=$(mktemp)
#echo $GCP_DEPLOY_KEY_SNAPSHOT >> $GCP_ACCOUNT_FILE
gcloud auth activate-service-account --key-file ~/Desktop/gcp_credential.json

gcloud config set project 'grakn-dev'


echo "Creating google cloud compute instance $INSTANCE_NAME..." | tee -a $LOG

#TODO: update metadata for project ssh keys
#for now it will keep trying to Updating project ssh metadata... and fail every time we try to ssh in it
gcloud compute instances create $INSTANCE_NAME     \
    --image benchmark-executor-image-2             \
    --image-project 'grakn-dev'                    \
    --machine-type n1-standard-16                  \
    --zone=$ZONE                                   \
    2>&1 | tee -a $LOG


echo "Waiting for $INSTANCE_NAME to be up and running..." | tee -a $LOG

RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='true' 2>&1 | tee -a $LOG
    RET=$?; # collect return code
done


#If the boot hasnt finished apt-get will fail
echo "Waiting for boot to finish..."
#gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='while [[ ! -f /var/lib/cloud/instance/boot-finished ]]; do echo -n . && sleep 2; done;'


#
#gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='systemctl stop apt-daily.service'
#
#
#gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='systemctl kill --kill-who=all apt-daily.service'
#gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="while ! (systemctl list-units --all apt-daily.service | egrep -q '(dead|failed)'); do sleep 1; done;"




sleep 120;

echo "lalallala"


gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="ps aux | grep -i apt"





echo "Installing git..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='sudo apt-get install git'

#echo "Downloading git-lfs..."
#gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | sudo bash'
#
#echo "Installing git-lfs..."
#gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='sudo apt-get install git-lfs'
#
#echo "Installing lfs..."
#gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='git lfs install --skip-smudge'

echo "Cloning BioGrakn..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="git clone https://github.com/graknlabs/biograkn"

echo "cd biograkn..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd /home/ubuntu/biograkn/"

gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="git lfs pull"



echo "Building Grakn core..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="bazel build @graknlabs_grakn_core//:assemble-linux-targz"

echo "Extracting Grakn core..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="mkdir dist && tar -xvzf bazel-genfiles/external/graknlabs_grakn_core/grakn-core-all-linux.tar.gz -C dist/"

echo "Starting Grakn..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="nohup dist/grakn-core-all-linux/grakn server start"

echo "Migrating BioGrakn..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="bazel run //migrator:migrator-bin"

echo "Stoping Grakn..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="dist/grakn-core-all-linux/grakn server stop"

echo "Renaming.."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="mv dist/grakn-core-all-linux dist/biograkn-linux"

echo "Taring BioGrakn.."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="tar -czf dist/biograkn-linux.tar.gz dist/biograkn-linux"

echo "Moving BioGrakn to ciclec ci.."
gcloud compute scp --recurse ubuntu@$INSTANCE_NAME:"~/dist" --zone=$ZONE ~/
