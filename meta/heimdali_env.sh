#!/bin/bash
export HEIMDALI_SECRET=`cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w ${1:-32} | head -n 1`
export HEIMDALI_API_HOME=$PARCELS_ROOT/$KAFKA_DIRNAME/usr/lib/heimdali-api
export HEIMDALI_UI_HOME=$PARCELS_ROOT/$KAFKA_DIRNAME/usr/lib/heimdali-ui