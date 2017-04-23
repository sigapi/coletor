#!/bin/bash

if [ ! -f ~/runonce ]
then

  export DEBIAN_FRONTEND=noninteractive # http://serverfault.com/a/670688

  # Pacotes
  apt-get install -yq \
    curl \
    openjdk-8-jdk \ #OpenJDK
    openjfx \ #OpenJFX
    tree \
    unzip \
    zip

  # Timezone
  timedatectl set-timezone America/Sao_Paulo

  # Locale
  sudo locale-gen pt_BR
  sudo locale-gen pt_BR.UTF-8
  sudo dpkg-reconfigure locales
  sudo update-locale LANG=pt_BR.UTF-8

  touch ~/runonce

fi
