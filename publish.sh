#!/bin/bash

set -xeu

HEIMDALI_VERSION=${HEIMDALI_VERSION:-$BITBUCKET_TAG}
PARCEL_BASE="cloudera-integration/parcel"
CSD_BASE="cloudera-integration/csd"
BUILD_BASE="cloudera-integration/build"
PUBLISH_DIR=${BUILD_BASE}/publish
DEPLOY_REPO=parcels-dev

mkdir -p ${BUILD_BASE}
mkdir -p ${PUBLISH_DIR}

package()
{
  ARTIFACT=$1
  BUILD_NAME=$(echo "${ARTIFACT}-${HEIMDALI_VERSION}" | tr a-z A-Z)
  READY_DIR=${BUILD_BASE}/${BUILD_NAME}

  mkdir -p ${READY_DIR}
  cp -R ${PARCEL_BASE}/${ARTIFACT}-meta ${READY_DIR}
  cp -r ${READY_DIR}/${ARTIFACT}-meta ${READY_DIR}/meta

  sed -i "s/0.1.5/${HEIMDALI_VERSION}/g" "${READY_DIR}/meta/parcel.json"

  mkdir -p ${READY_DIR}/usr/lib/heimdali-api
  mkdir -p ${READY_DIR}/usr/lib/heimdali-api-tests
  mkdir -p ${READY_DIR}/usr/bin/

  # API artifacts
  cp api/target/scala-2.12/heimdali-api.jar ${READY_DIR}/usr/lib/heimdali-api/
  cp integration-test/target/scala-2.12/heimdali-integration-tests.jar ${READY_DIR}/usr/lib/heimdali-api-tests/
  cp integration-test/target/scala-2.12/heimdali-test-dependencies.jar ${READY_DIR}/usr/lib/heimdali-api-tests/
  cp common/target/scala-2.12/heimdali-common-tests.jar ${READY_DIR}/usr/lib/heimdali-api-tests/

  # UI artifacts
  cp -r dist ${READY_DIR}/usr/lib/heimdali-ui
  cp -R public/images ${READY_DIR}/usr/lib/heimdali-ui/
  cp -R public/fonts ${READY_DIR}/usr/lib/heimdali-ui/

  # Control script
  cp bin/control.sh ${READY_DIR}/usr/bin/


  pushd ${BUILD_BASE}
  tar cvf ${BUILD_NAME}-el6.parcel ${BUILD_NAME}
  popd
  cp ${BUILD_BASE}/${BUILD_NAME}-el6.parcel ${PUBLISH_DIR}
  pushd ${PUBLISH_DIR}
  ln -sf ${BUILD_NAME}-el6.parcel ${BUILD_NAME}-el7.parcel
  ln -sf ${BUILD_NAME}-el6.parcel ${BUILD_NAME}-xenial.parcel
  sha1sum $BUILD_NAME-el6.parcel | cut -d' ' -f1  > ${BUILD_NAME}-el6.parcel.sha
  sha1sum $BUILD_NAME-el7.parcel | cut -d' ' -f1  > ${BUILD_NAME}-el7.parcel.sha
  sha1sum $BUILD_NAME-xenial.parcel | cut -d' ' -f1  > ${BUILD_NAME}-xenial.parcel.sha

  popd
}

csd()
{
  cp -R ${CSD_BASE} ${BUILD_BASE}
  pushd ${BUILD_BASE}/csd
  sed -i -e "s/0.1.0/${HEIMDALI_VERSION}/g" descriptor/service.sdl
  jar cvf HEIMDALI-${HEIMDALI_VERSION}.jar `find . -mindepth 1 -not -path "./.git*"`
  popd
  cp ${BUILD_BASE}/csd/HEIMDALI-${HEIMDALI_VERSION}.jar ${PUBLISH_DIR}
}

manifest()
{
  python /usr/src/make_manifest.py ${PUBLISH_DIR} || build-support/bin/manifest ${PUBLISH_DIR}
}

ship()
{
  pushd ${PUBLISH_DIR}
  READY_DIR=$(echo "${HEIMDALI_VERSION}" | tr a-z A-Z)
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T manifest.json https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/manifest.json
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T HEIMDALI-${HEIMDALI_VERSION}.jar https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/HEIMDALI-${HEIMDALI_VERSION}.jar

  push heimdali

  popd
}

push()
{
  ARTIFACT=$1
  READY_DIR=$(echo "${ARTIFACT}-${HEIMDALI_VERSION}" | tr a-z A-Z)
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${READY_DIR}-el6.parcel https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/${READY_DIR}-el6.parcel
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${READY_DIR}-el7.parcel https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/${READY_DIR}-el7.parcel
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${READY_DIR}-xenial.parcel https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/${READY_DIR}-xenial.parcel
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${READY_DIR}-el6.parcel.sha https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/${READY_DIR}-el6.parcel.sha
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${READY_DIR}-el7.parcel.sha https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/${READY_DIR}-el7.parcel.sha
  curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${READY_DIR}-xenial.parcel.sha https://repository.phdata.io/artifactory/$DEPLOY_REPO/com/heimdali/${HEIMDALI_VERSION}/${READY_DIR}-xenial.parcel.sha
}

case $1 in
  parcel)
    package $2
    ;;
  csd)
    csd
    ;;
  manifest)
    manifest
    ;;
  ship)
    ship
    ;;
esac
