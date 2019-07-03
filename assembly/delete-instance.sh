#!/bin/bash

if [ $# -ne 1 ]
then
    echo "Require 1 argument:"
    echo "Usage: ./delete_instance.sh <instance name>"
    exit 1;
fi

ZONE=us-east1-b

INSTANCE_NAME=$1

echo "Deleting $INSTANCE_NAME..."
yes | gcloud compute instances delete $INSTANCE_NAME --zone=$ZONE --delete-disks=all