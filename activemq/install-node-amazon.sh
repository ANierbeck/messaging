#!/bin/bash

if [ $# == 0 ]
then
        echo "ID fehlt: Bitte config-nummer angeben"
        exit
fi

sudo apt-get update

# install java 7
sudo apt-get -y install openjdk-7-jre
sudo apt-get install htop

if [ ! -f /tmp/activemq.tar.gz ] 
then
        # download artemis install file
        sudo wget http://apache.lauf-forum.at/activemq/5.13.0/apache-activemq-5.13.0-bin.tar.gz -O /tmp/activemq.tar.gz
fi


# unzip activemq
if [ ! -d /opt/activemq ] 
then
        sudo mkdir /opt/activemq
        sudo tar -xvf /tmp/activemq.tar.gz -C /opt/activemq
fi

cd /opt/activemq/apache-activemq-5.13.0/

sudo cp /home/ubuntu/messaging/activemq/config/amazon$1.xml /opt/activemq/apache-activemq-5.13.0/conf/activemq.xml

# increase heap
sudo sed -i 's/Xms64M/Xms2096m/g' /opt/activemq/apache-activemq-5.13.0/bin/env
sudo sed -i 's/Xmx1G/Xmx2096m/g' /opt/activemq/apache-activemq-5.13.0/bin/env



# (re)start the broker
sudo "/opt/activemq/apache-activemq-5.13.0/bin/activemq" stop
sudo "/opt/activemq/apache-activemq-5.13.0/bin/activemq" start &
