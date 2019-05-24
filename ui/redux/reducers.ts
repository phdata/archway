import { combineReducers } from 'redux-immutable';
import cluster from '../containers/Navigation/reducers';
import login from '../containers/Login/reducers';
import details from '../containers/WorkspaceDetails/reducers';
import listing from '../containers/WorkspaceListing/reducers';
import risk from '../containers/RiskListing/reducers';
import operations from '../containers/OpsListing/reducers';
import request from '../containers/WorkspaceRequest/reducers';
import { reducer as form } from 'redux-form/immutable';
import home from '../containers/Home/reducers';

const reducers = combineReducers<any>({
  login,
  listing,
  risk,
  operations,
  request,
  cluster,
  details,
  form,
  home,
});

export default reducers;
