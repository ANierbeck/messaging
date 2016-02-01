#!/bin/bash



## AMAZON EDITION : : : :: :  ::  : ##
#sudo /bin/su -c "echo 'echo 10.0.3.101 rabbitmq1' > /etc/hosts"
#sudo /bin/su -c "echo 'echo 10.0.3.102 rabbitmq2' > /etc/hosts"
## ::: : : .: ::: .:: .:: ::  ::  : ##


sudo apt-get update

# install java 7
sudo apt-get install htop

sudo apt-get install -y rabbitmq-server

if [ ! -f /vagrant/tmp/rabbitmq_server.deb ] 
then
        # download rabbitmq deb file
        sudo wget https://packages.erlang-solutions.com/erlang/esl-erlang/FLAVOUR_1_general/esl-erlang_18.2-1~ubuntu~precise_amd64.deb -O /vagrant/tmp/otp.deb
fi


# install from deb file
sudo dpkg -i /vagrant/tmp/rabbitmq_server.deb


# kill all running rabbitmq processes because I have no idea how to stop them correctly ...
sudo pkill -f rabbitmq
sudo pkill -f epmd




# copy the "rabbitmq cookie", which is like a cluster name and must be identical across the cluster
sudo /bin/su -c "echo 'IDUFSZKDKLDTBGQRFLAR' > /var/lib/rabbitmq/.erlang.cookie"






#sudo cp /vagrant/config/$HOSTNAME.xml /opt/activemq/apache-activemq-5.13.0/conf/activemq.xml
# increase heap
#sudo sed -i 's/Xms64M/Xms2096m/g' /opt/activemq/apache-activemq-5.13.0/bin/env
#sudo sed -i 's/Xmx1G/Xmx2096m/g' /opt/activemq/apache-activemq-5.13.0/bin/env

# (re)start the broker
sudo rabbitmq-server -detached
