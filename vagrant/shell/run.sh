#!/bin/bash

fuser -v -n tcp 80 && fuser -k 80/tcp

dir=/sigapi
mkdir -p ${dir}
cp /tmp/sigapi/coletor.jar ${dir}/

nohup java -Dspring.profiles.active=prod -jar ${dir}/coletor.jar &
