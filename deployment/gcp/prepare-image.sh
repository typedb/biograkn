#!/bin/bash
image=$1
PROJECT="grakn-dev"
ZONE="us-central1-f"
CLEAN_INSTANCE="${image}-clean"
BOOT_INSTANCE="${image}-boot"
LICENSED_IMAGE="${image}-licensed"
IMAGE_FAMILY="grakn-kgms-premium"

# Create vm from ui from packer image, mark option to not delete boot disk
echo "Creating instance $CLEAN_INSTANCE from image: ${image}"
gcloud compute instances create $CLEAN_INSTANCE --image $image --no-boot-disk-auto-delete --zone $ZONE
sleep 30

gcloud compute ssh $CLEAN_INSTANCE --project $PROJECT --zone $ZONE --command "sudo userdel -r lolski"

# Delete it

echo "Deleting the instance..."
gcloud compute instances delete $CLEAN_INSTANCE --zone $ZONE --quiet

# Create another vm with the boot disk attached

echo "Creating new instance $BOOT_INSTANCE with boot disk attached."
gcloud compute instances create $BOOT_INSTANCE --disk name=$CLEAN_INSTANCE --zone $ZONE
sleep 30

# Mount boot

echo "Mounting boot..."
gcloud compute ssh $BOOT_INSTANCE --project $PROJECT --zone $ZONE --command "sudo mkdir -p /mnt/disks/boot && sudo mount -o discard,defaults /dev/sdb1 /mnt/disks/boot"

# Clean boot

echo "Cleaning boot..."
gcloud compute ssh $BOOT_INSTANCE --project $PROJECT --zone $ZONE --command "sudo rm -rf /mnt/disks/boot/home/kasper"
gcloud compute ssh $BOOT_INSTANCE --project $PROJECT --zone $ZONE --command "sudo rm -rf /mnt/disks/boot/home/lolski"

# kill instance

echo "Killing the instance..."
gcloud compute instances delete $BOOT_INSTANCE --zone $ZONE --quiet

# Create image from disk

echo "Creating image from boot disk..."
LICENSE="https://www.googleapis.com/compute/v1/projects/grakn-public/global/licenses/grakn-kgms-premium"
# TODO(vmax): also add license-byol, i.e.
# LICENSE="https://www.googleapis.com/compute/v1/projects/grakn-public/global/licenses/grakn-kgms-premium-byol"
gcloud compute images create $LICENSED_IMAGE --family=$IMAGE_FAMILY --source-disk=$CLEAN_INSTANCE --source-disk-zone=$ZONE --licenses=$LICENSE

echo "Deleting disk..."
gcloud compute disks delete $CLEAN_INSTANCE --zone $ZONE --quiet