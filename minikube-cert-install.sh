#!/bin/bash

curr_ip=$(ip -o -4 addr show dev wlo1 | awk '{print $4}' | cut -d/ -f1)

minikube ssh -- "echo '$curr_ip registry.local' | sudo tee -a /etc/hosts"

#

minikube cp ./certs/domain.crt /tmp/registry.local.crt

minikube ssh -- "sudo mkdir -p /etc/docker/certs.d/registry.local && \
                 sudo mv /tmp/registry.local.crt /etc/docker/certs.d/registry.local/ca.crt && \
                 sudo systemctl restart docker"

#

minikube ssh -- "if [ -d /usr/local/share/ca-certificates ]; then \
                   sudo cp /etc/docker/certs.d/registry.local/ca.crt /usr/local/share/ca-certificates/registry.local.crt && \
                   sudo update-ca-certificates || true; \
                 fi"
