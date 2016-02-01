#!/bin/bash

sudo rabbitmqctl add_user admin admin
sudo rabbitmqctl set_permissions -p / admin  ".*"  ".*"  ".*"


