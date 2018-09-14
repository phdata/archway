import { all, fork } from 'redux-saga/effects';

import members from './Members/sagas';
import status from './Status/sagas';
import details from './WorkspaceDetails/sagas';
import listing from './WorkspaceList/sagas';
import topics from './Topics/sagas';
import applications from './Applications/sagas';

export default function* root() {
  yield all([
    fork(members),
    fork(status),
    fork(details),
    fork(listing),
    fork(topics),
    fork(applications),
  ]);
}