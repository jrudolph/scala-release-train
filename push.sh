#!/bin/sh

set -x
set -e

IMAGE=$1
VERSION=$2

sudo docker tag $IMAGE registry.virtual-void.net/jrudolph/release-train:$VERSION
sudo docker push registry.virtual-void.net/jrudolph/release-train:$VERSION
git tag -a $VERSION -m "pushed $VERSION"
git push --tags