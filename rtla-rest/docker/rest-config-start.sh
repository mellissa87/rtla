#!/bin/bash

export CONFIG="conf/config.yml"

if [[ -n "${SSL_ENABLED}" ]] ; then
  sed -i -e "s/isSSLEnabled: false/isSSLEnabled: ${SSL_ENABLED}/" "${CONFIG}"
fi

exec "$@"
