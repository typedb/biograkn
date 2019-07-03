#!/bin/bash

INSTANCE_NAME="assemble-biograkn-linux"
ZONE="us-east1-b"

mkdir ~/logs
LOG=~/logs/launch_executor_$INSTANCE_NAME.log


export GCP_ACCOUNT_FILE=$(mktemp)
echo $GCP_DEPLOY_KEY_SNAPSHOT >> $GCP_ACCOUNT_FILE
gcloud auth activate-service-account --key-file $GCP_ACCOUNT_FILE

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
echo "Waiting for boot to finish..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='while [[ ! -f /var/lib/cloud/instance/boot-finished ]]; do; echo -n . && sleep 2; done;' 2>&1 | tee -a $LOG

echo "Installing git..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='sudo apt-get install git' 2>&1 | tee -a $LOG

echo "Downloading git-lfs..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | sudo bash' 2>&1 | tee -a $LOG

echo "Installing git-lfs..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='sudo apt-get install git-lfs' 2>&1 | tee -a $LOG

echo "Installing lfs..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='git lfs install --skip-smudge' 2>&1 | tee -a $LOG

echo "Cloning BioGrakn..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="git clone https://github.com/$CIRCLE_PROJECT_USERNAME/$CIRCLE_PROJECT_REPONAME" 2>&1 | tee -a $LOG

echo "Cloning BioGrakn..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn" 2>&1 | tee -a $LOG

echo "Building Grakn core..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="bazel build @graknlabs_grakn_core//:assemble-linux-targz" 2>&1 | tee -a $LOG

echo "Extracting Grakn core..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="mkdir dist && tar -xvzf bazel-genfiles/external/graknlabs_grakn_core/grakn-core-all-linux.tar.gz -C dist/" 2>&1 | tee -a $LOG

echo "Starting Grakn..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="nohup dist/grakn-core-all-linux/grakn server start" 2>&1 | tee -a $LOG

echo "Migrating BioGrakn..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="bazel run //migrator:migrator-bin" 2>&1 | tee -a $LOG

echo "Stoping Grakn..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="dist/grakn-core-all-linux/grakn server stop" 2>&1 | tee -a $LOG

echo "Renaming.." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="mv dist/grakn-core-all-linux dist/biograkn-linux" 2>&1 | tee -a $LOG

echo "Taring BioGrakn.." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="tar -czf dist/biograkn-linux.tar.gz dist/biograkn-linux" 2>&1 | tee -a $LOG

echo "Moving BioGrakn to ciclec ci.." | tee -a $LOG
gcloud compute scp --recurse ubuntu@$INSTANCE_NAME:"~/dist" --zone=$ZONE ~/
