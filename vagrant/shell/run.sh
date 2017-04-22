#!/bin/bash

dir=/sigapi
fuser -v -n tcp 80 && fuser -k 80/tcp

rm -rf ${dir}
mkdir -p ${dir}
cp /tmp/sigapi/coletor.jar ${dir}/

nohup java -Dfile.encoding=UTF-8 -Dserver.port=80 -jar ${dir}/coletor.jar > ${dir}/coletor-nohup.out &
