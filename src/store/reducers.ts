import { combineReducers } from 'redux-immutable';

import auth from '../Auth/reducers';
import cluster from '../Navigation/reducers';
import listing from '../WorkspaceListing/reducers';
import request from '../WorkspaceRequest/reducers';
import details from '../WorkspaceDetails/reducers';

const reducers = combineReducers({
  auth,
  listing,
  request,
  cluster,
  details,
});

export default reducers;