#!/bin/bash

sudo apt-get update

# install java 7
sudo apt-get -y install openjdk-7-jre
sudo apt-get install htop

if [ ! -f /vagrant/tmp/activemq.tar.gz ] 
then
        # download artemis install file
        sudo wget http://apache.lauf-forum.at/activemq/5.13.0/apache-activemq-5.13.0-bin.tar.gz -O /vagrant/tmp/activemq.tar.gz
fi


# unzip activemq
if [ ! -d /opt/activemq ] 
then
        sudo mkdir /opt/activemq
        sudo tar -xvf /vagrant/tmp/activemq.tar.gz -C /opt/activemq
fi

cd /opt/activemq/apache-activemq-5.13.0/


sudo cp /vagrant/config/$HOSTNAME.xml /opt/activemq/apache-activemq-5.13.0/conf/activemq.xml

#sudo sed -i 's/-classpath \"\$CLASSPATH\" \\/-classpath \"\$CLASSPATH\" -Xmx2096m -Xms2096m \\/g' /etc/broker/bin/artemis


# (re)start the broker
sudo "/opt/activemq/apache-activemq-5.13.0/bin/activemq" stop
sudo "/opt/activemq/apache-activemq-5.13.0/bin/activemq" start &
