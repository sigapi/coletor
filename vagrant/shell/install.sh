#!/bin/bash

export DEBIAN_FRONTEND=noninteractive # http://serverfault.com/a/670688

apt-get install -yq openjdk-8-jdk openjfx

echo IP: $(curl -sS ipinfo.io/ip)

whoami

mkdir -p /sigapi
# chown --recursive vagrant /sigapi
