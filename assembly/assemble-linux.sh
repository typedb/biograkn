#!/bin/bash

INSTANCE_NAME="assemble-biograkn-linux"
ZONE="us-east1-b"


export GCP_ACCOUNT_FILE=$(mktemp)
echo $GCP_DEPLOY_KEY_SNAPSHOT >> $GCP_ACCOUNT_FILE
gcloud auth activate-service-account --key-file $GCP_ACCOUNT_FILE

gcloud config set project 'grakn-dev'


echo "Creating google cloud compute instance $INSTANCE_NAME..."

#TODO: update metadata for project ssh keys
#for now it will keep trying to Updating project ssh metadata... and fail every time we try to ssh in it
gcloud compute instances create $INSTANCE_NAME     \
    --image benchmark-executor-image-2             \
    --image-project 'grakn-dev'                    \
    --machine-type n1-standard-16                  \
    --zone=$ZONE                                   \
    2>&1 | tee -a $LOG


echo "Waiting for $INSTANCE_NAME to be up and running..."

RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='true'
    RET=$?; # collect return code
done

sleep 120;

echo "Installing git..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='sudo apt-get install git'

echo "Cloning BioGrakn..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="git clone --branch refactor-ci https://${REPO_GITHUB_TOKEN}@github.com/graknlabs/biograkn"

echo "Downloading git-lfs..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | sudo bash'

echo "Installing git-lfs..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='sudo apt-get install git-lfs'
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='git lfs install'

echo "Pulling lfs objects..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && git lfs pull"

echo "Building Grakn core..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && bazel build @graknlabs_grakn_core//:assemble-linux-targz"

echo "Extracting Grakn core..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && mkdir dist && tar -xvzf bazel-genfiles/external/graknlabs_grakn_core/grakn-core-all-linux.tar.gz -C dist/"

echo "Starting Grakn..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && nohup dist/grakn-core-all-linux/grakn server start"

 echo "Migrating BioGrakn..."
 gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && bazel run //migrator:migrator-bin"

 echo "Stoping Grakn..."
 gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && dist/grakn-core-all-linux/grakn server stop"

# echo "Renaming.."
# gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && mv dist/grakn-core-all-linux dist/biograkn-linux"

# echo "Taring BioGrakn.."
# gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command="cd biograkn/ && tar -czf dist/biograkn-linux.tar.gz dist/biograkn-linux"

# echo "Moving BioGrakn to ciclec ci.."
# gcloud compute scp --recurse ubuntu@$INSTANCE_NAME:"~/dist" --zone=$ZONE ~/
