import { combineReducers } from 'redux-immutable';
import cluster from '../containers/Navigation/reducers';
import login from '../containers/Login/reducers';
import details from '../containers/WorkspaceDetails/reducers';
import listing from '../containers/WorkspaceListing/reducers';
import request from '../containers/WorkspaceRequest/reducers';
import { reducer as form } from 'redux-form/immutable';

const reducers = combineReducers({
  login,
  listing,
  request,
  cluster,
  details,
  form,
});

export default reducers;
