#!/bin/bash

sudo rabbitmqctl stop_app
sudo rabbitmqctl join_cluster rabbit@rabbitmq1
sudo rabbitmqctl start_app
