#!/bin/bash

kubectl create secret tls registry-tls \
  --cert=certs/domain.crt \
  --key=certs/domain.key \

