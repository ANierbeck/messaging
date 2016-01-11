#!/bin/bash

sudo apt-get update


# install java 7
sudo apt-get -y install openjdk-7-jre

if [ ! -f /vagrant/tmp/artemis.tar.gz ] 
then
        # download artemis install file
        sudo wget http://apache.lauf-forum.at/activemq/activemq-artemis/1.2.0/apache-artemis-1.2.0-bin.tar.gz -O /vagrant/tmp/artemis.tar.gz
fi

# create required folders
sudo mkdir /etc/broker

# unzip artemis
if [ ! -d /opt/artemis ] 
then
        sudo mkdir /opt/artemis
        sudo tar -xvf /vagrant/tmp/artemis.tar.gz -C /opt/artemis
fi

cd /opt/artemis/apache-artemis-1.2.0/

# create a broker
sudo ./bin/artemis create --allow-anonymous  --password admin --user admin --directory /etc/broker --host 0.0.0.0 --force


### adjust configuration

# replace "localhost" with "0.0.0.0" for external port forwarding
sudo sed -i 's/localhost/0.0.0.0/g' /etc/broker/etc/bootstrap.xml

#sudo sed -i 's/logger\.level=INFO/logger\.level=DEBUG/g' /etc/broker/etc/logging.properties


sudo cp /vagrant/config/$HOSTNAME.xml /etc/broker/etc/broker.xml

sudo ip route list | grep '224' &> /dev/null
if [ ! $? == 0 ]; then
        sudo ip route add 224.0.0.0/4 dev eth1
fi

# restart the broker
#sudo "/etc/broker/bin/artemis" stop &
#sudo "/etc/broker/bin/artemis" run &
