#!/bin/bash
export HEIMDALI_SECRET=`date +%s | sha256sum | base64 | head -c 32`
export HEIMDALI_API_HOME=$PARCELS_ROOT/$PARCEL_DIRNAME/usr/lib/heimdali-api
export HEIMDALI_UI_HOME=$PARCELS_ROOT/$PARCEL_DIRNAME/usr/lib/heimdali-ui