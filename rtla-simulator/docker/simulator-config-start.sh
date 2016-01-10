#!/bin/bash

export IP=$(hostname -i)
export CONFIG="conf/config.yml"
export LOGBACK="conf/logback.xml"

function ip_hash() {
  hash=$(md5sum <<< "$1" | cut -b 1-6)
  echo $((0x${hash%% *}))
}

sed -i -e "s/testhost/$(ip_hash ${IP})/" "${LOGBACK}"

if [[ -n "${LOGDIR_NUM}" ]] ; then
  sed -i -e "s/inputDir: \/simulation-data\/logs1/inputDir: \/simulation-data\/logs${LOGDIR_NUM}/" "${CONFIG}"
fi

if [[ -n "${DELAY}" ]] ; then
  sed -i -e "s/delay: 1000/delay: ${DELAY}/" "${CONFIG}"
fi

if [[ -n "${LOOPS}" ]] ; then
  sed -i -e "s/loops: 0/loops: ${LOOPS}/" "${CONFIG}"
fi

exec "$@"
