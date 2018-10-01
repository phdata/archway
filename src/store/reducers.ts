import { combineReducers } from 'redux-immutable';
import cluster from '../components/Navigation/reducers';
import login from '../pages/Login/reducers';
import details from '../pages/WorkspaceDetails/reducers';
import listing from '../pages/WorkspaceListing/reducers';
import request from '../pages/WorkspaceRequest/reducers';
import { reducer as form } from 'redux-form';

const reducers = combineReducers({
  login,
  listing,
  request,
  cluster,
  details,
  form,
});

export default reducers;
