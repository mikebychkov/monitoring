#!/bin/bash

mkdir -p certs

openssl req -newkey rsa:4096 -nodes -sha256 -keyout certs/domain.key \
  -x509 -days 365 -out certs/domain.crt \
  -subj "/CN=registry.local" \
  -addext "subjectAltName=DNS:registry.local"

sudo cp certs/domain.crt /etc/pki/ca-trust/source/anchors/
sudo update-ca-trust
sudo systemctl restart docker


