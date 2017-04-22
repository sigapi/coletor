#!/bin/bash

if [ ! -f ~/runonce ]
then

  export DEBIAN_FRONTEND=noninteractive # http://serverfault.com/a/670688

  # OpenJDK + OpenJFXX
  apt-get install -yq openjdk-8-jdk openjfx

  # Timezone
  timedatectl set-timezone America/Sao_Paulo

  # Locale
  sudo locale-gen pt_BR
  sudo locale-gen pt_BR.UTF-8
  sudo dpkg-reconfigure locales
  sudo update-locale LANG=pt_BR.UTF-8

  touch ~/runonce

fi
