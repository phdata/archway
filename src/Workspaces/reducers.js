import { combineReducers } from 'redux';

import members from './Members/reducers';
import request from './Request/reducers';
import status from './Status/reducers';
import details from './WorkspaceDetails/reducers';
import listing from './WorkspaceList/reducers';
import topics from './Topics/reducers';
import applications from './Applications/reducers';

export default combineReducers({
  members,
  request,
  status,
  details,
  listing,
  topics,
  applications,
});