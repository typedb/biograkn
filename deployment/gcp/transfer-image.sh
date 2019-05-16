#!/bin/bash
SOURCE_IMAGE=$1
SOURCE_PROJECT=$2
DEST_IMAGE=$3
DEST_PROJECT=$4
IMAGE_FAMILY="grakn-kgms-premium"
gcloud compute images create $DEST_IMAGE --project $DEST_PROJECT --source-image=$SOURCE_IMAGE --source-image-project=$SOURCE_PROJECT --family=$IMAGE_FAMILY 
