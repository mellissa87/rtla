#!/bin/bash

host=`hostname -i`
config="${SIM_HOME}/conf/config.yml"
logback="${SIM_HOME}/conf/logback.xml"

cd "${SIM_HOME}"

sed -i -e "s/testhost/${host}/" "${logback}"

if [[ -n "${LOGDIR_NUM}" ]] ; then
  sed -i -e "s/inputDir: \/simulation-data\/logs1/inputDir: \/simulation-data\/logs${LOGDIR_NUM}/" "${config}"
fi

if [[ -n "${DELAY}" ]] ; then
  sed -i -e "s/delay: 1000/delay: ${DELAY}/" "${config}"
fi

if [[ -n "${LOOPS}" ]] ; then
  sed -i -e "s/loops: 0/loops: ${LOOPS}/" "${config}"
fi

exec "$@"
