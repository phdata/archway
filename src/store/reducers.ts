import { combineReducers } from 'redux-immutable';
import cluster from '../components/Navigation/reducers';
import auth from '../pages/Auth/reducers';
import details from '../pages/WorkspaceDetails/reducers';
import listing from '../pages/WorkspaceListing/reducers';
import request from '../pages/WorkspaceRequest/reducers';

const reducers = combineReducers({
  auth,
  listing,
  request,
  cluster,
  details,
});

export default reducers;