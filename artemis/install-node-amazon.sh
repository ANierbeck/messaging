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

if [ ! -f /tmp/artemis.tar.gz ] 
then
        # download artemis install file
        sudo wget http://apache.lauf-forum.at/activemq/activemq-artemis/1.2.0/apache-artemis-1.2.0-bin.tar.gz -O /tmp/artemis.tar.gz
fi

# create required folders
sudo mkdir /etc/broker

# unzip artemis
if [ ! -d /opt/artemis ] 
then
        sudo mkdir /opt/artemis
        sudo tar -xvf /tmp/artemis.tar.gz -C /opt/artemis
fi

cd /opt/artemis/apache-artemis-1.2.0/

# create a broker
sudo ./bin/artemis create --allow-anonymous  --password admin --user admin --directory /etc/broker --host 0.0.0.0 --force

### adjust configuration

# replace "localhost" with "0.0.0.0" for external port forwarding
sudo sed -i 's/localhost/0.0.0.0/g' /etc/broker/etc/bootstrap.xml

#sudo sed -i 's/logger\.level=INFO/logger\.level=DEBUG/g' /etc/broker/etc/logging.properties

sudo cp config/amazon$1.xml /etc/broker/etc/broker.xml

# restart the broker
#sudo "/etc/broker/bin/artemis" stop &
sudo "/etc/broker/bin/artemis" run &

# START:
# sudo "/etc/broker/bin/artemis" run &
# KILL:
# sudo kill -9 $(ps -aef | grep "apache-artemis-1.2.0" | grep root | cut -d " " -f 7)

