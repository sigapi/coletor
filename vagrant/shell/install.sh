#!/bin/bash

if [ ! -f ~/runonce ]
then

  export DEBIAN_FRONTEND=noninteractive # http://serverfault.com/a/670688

  # Pacotes
  apt-get install -yq \
    curl \
    openjdk-8-jdk \
    openjfx \
    tree \
    unzip \
    zip

  # Timezone
  timedatectl set-timezone America/Sao_Paulo

  # Locale
  locale-gen pt_BR
  locale-gen pt_BR.UTF-8
  dpkg-reconfigure locales
  update-locale LANG=pt_BR.UTF-8

  touch ~/runonce

fi
