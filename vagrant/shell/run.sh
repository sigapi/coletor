#!/bin/bash

# Para a execução anterior
echo "Execução anterior..."
fuser -k -TERM -n tcp 80 -s

# Prepara o diretório
echo "Diretório..."
dir=/sigapi
mkdir -p ${dir}
cd ${dir}
cp /tmp/sigapi/coletor.jar .

# Salva cópia dos logs
echo "Logs..."
if [ -d logs ]; then
    cd logs
    if [ -f $FILE ]; then
        cp coletor.{log,"$(date +%Y%m%d-%H%M%S)".log}
        rm -rf coletor.log
    fi
fi

# Iniciando execução atual
echo "Iniciando..."
cd ${dir}
nohup java -Dthin.root=${dir}/mvn -Dspring.profiles.active=prod -jar coletor.jar &
sleep 5s
