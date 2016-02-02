#!/bin/bash

sudo rabbitmqctl stop_app
sudo rabbitmqctl join_cluster rabbit@rabbitmq1

# AMAZON: rabbitmqctl join_cluster rabbit@ip-172-31-0-153
sudo rabbitmqctl start_app
