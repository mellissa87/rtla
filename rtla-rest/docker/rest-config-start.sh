#!/bin/bash

config="conf/config.yml"

if [[ -n "${SSL_ENABLED}" ]] ; then
  sed -i -e "s/isSSLEnabled: true/isSSLEnabled: ${SSL_ENABLED}/" "${config}"
fi

exec "$@"
