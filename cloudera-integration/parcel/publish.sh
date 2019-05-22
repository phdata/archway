#!/bin/bash -x

prepare()
{
  ARTIFACT=$1
  READY_DIR=$(echo "${ARTIFACT}-${BITBUCKET_TAG}" | tr a-z A-Z)
  echo $READY_DIR

  mkdir -p $READY_DIR
  cp -R $ARTIFACT-meta $READY_DIR
  mv $READY_DIR/$ARTIFACT-meta $READY_DIR/meta

  sed -i "s/0.1.5/${BITBUCKET_TAG}/g" $READY_DIR/meta/parcel.json

  mkdir -p $READY_DIR/usr/lib/heimdali-api
  chown -R 10000:10000 $READY_DIR

  mv api/target/scala-2.12/heimdali-api.jar $READY_DIR/usr/lib/heimdali-api/heimdali-api.jar
  mv dist $READY_DIR/usr/lib/heimdali-ui
  mv public/images $READY_DIR/usr/lib/heimdali-ui/
  mv public/fonts $READY_DIR/usr/lib/heimdali-ui/

  tar cvf $READY_DIR-el6.parcel $READY_DIR
  mkdir -p publish
  mv $READY_DIR-el6.parcel publish/
  pushd publish
  ln -s $READY_DIR-el6.parcel $READY_DIR-el7.parcel
  ln -s $READY_DIR-el6.parcel $READY_DIR-xenial.parcel
  popd
}

ship()
{
  cd publish

  python /usr/src/make_manifest.py .

  READY_DIR=$(echo "${ARTIFACT}-${BITBUCKET_TAG}" | tr a-z A-Z)
  curl -u$ARTIFACTORY_USER:$ARTIFACTORY_TOKEN -T manifest.json https://repository.phdata.io/artifactory/parcels-release/com/heimdali/${BITBUCKET_TAG}/manifest.json

  push heimdali
}

push()
{
  ARTIFACT=$1
  READY_DIR=$(echo "${ARTIFACT}-${BITBUCKET_TAG}" | tr a-z A-Z)
  curl -u$ARTIFACTORY_USER:$ARTIFACTORY_TOKEN -T $READY_DIR-el6.parcel https://repository.phdata.io/artifactory/parcels-release/com/heimdali/${BITBUCKET_TAG}/$READY_DIR-el6.parcel
  curl -u$ARTIFACTORY_USER:$ARTIFACTORY_TOKEN -T $READY_DIR-el7.parcel https://repository.phdata.io/artifactory/parcels-release/com/heimdali/${BITBUCKET_TAG}/$READY_DIR-el7.parcel
  curl -u$ARTIFACTORY_USER:$ARTIFACTORY_TOKEN -T $READY_DIR-xenial.parcel https://repository.phdata.io/artifactory/parcels-release/com/heimdali/${BITBUCKET_TAG}/$READY_DIR-xenial.parcel
}

case $1 in
  prepare)
    prepare $2
    ;;
  ship)
    ship
    ;;
esac