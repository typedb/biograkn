#!/bin/bash

set -ex

deployment_mode_param="$1"
packer_debug_param="$2"

[[ $(readlink $0) ]] && path=$(readlink $0) || path=$0
KGMS_HOME=$(cd "$(dirname "${path}")/../.." && pwd -P)
DEPLOY_GCP_IMAGE_HOME=$(cd "$(dirname "${path}")" && pwd -P)

if [[ $deployment_mode_param = "clean" ]]; then
    cd "$KGMS_HOME"
    bazel build //:assemble-mac-zip
    cp -f "$KGMS_HOME/bazel-genfiles/grakn-kgms-all-mac.zip" "$DEPLOY_GCP_IMAGE_HOME/files/"
    cd "$DEPLOY_GCP_IMAGE_HOME"
    image=$( packer build "$packer_debug_param" build-steps.json | tail -1 | sed -n "s/.*\(grakn-kgms-test-1-5-[0-9]*-[0-9]*-[0-9]*-[0-9]*\).*/\1/p" )
    echo "Created image name: $image"
elif [[ $deployment_mode_param = "licensed" ]]; then
    cd "$KGMS_HOME"
    bazel build //:assemble-mac-zip
    cp "$KGMS_HOME/bazel-genfiles/grakn-kgms-all-mac.zip" "$DEPLOY_GCP_IMAGE_HOME/files/"
    cd "$DEPLOY_GCP_IMAGE_HOME"
    image=$( packer build "$packer_debug_param" build-steps.json | tail -1 | sed -n "s/.*\(grakn-kgms-test-1-5-[0-9]*-[0-9]*-[0-9]*-[0-9]*\).*/\1/p" )
    echo "Created image name: $image"
    ./prepare-image.sh $image
elif [[ $deployment_mode_param = "release" ]]; then
    cd "$KGMS_HOME"
    bazel build //:assemble-mac-zip
    cp "$KGMS_HOME/bazel-genfiles/grakn-kgms-all-mac.zip" "$DEPLOY_GCP_IMAGE_HOME/files/"
    cd "$DEPLOY_GCP_IMAGE_HOME"
    image=$( packer build "$packer_debug_param" build-steps.json | tail -1 | sed -n "s/.*\(grakn-kgms-test-1-5-[0-9]*-[0-9]*-[0-9]*-[0-9]*\).*/\1/p" )
    echo "Created image name: $image"
    ./prepare-image.sh $image
    ./transfer-image.sh ${image}-licensed grakn-dev ${image} grakn-public
else
    echo "Usage: ./deploy.sh [clean|licensed|release] [debug|-color=true]"
fi
