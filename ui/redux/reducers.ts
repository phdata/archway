import { fromJS } from 'immutable';
import { combineReducers } from 'redux-immutable';
import login from '../containers/Login/reducers';
import details from '../containers/WorkspaceDetails/reducers';
import listing from '../containers/WorkspaceListing/reducers';
import risk from '../containers/RiskListing/reducers';
import operations from '../containers/OpsListing/reducers';
import request from '../containers/WorkspaceRequest/reducers';
import manage from '../containers/Manage/reducers';
import { reducer as form } from 'redux-form/immutable';
import home from '../containers/Home/reducers';
import templates from '../containers/CustomWorkspaces/reducers';
import { FEATURE_FLAG } from './actions';

const initialConfigState = fromJS({
  featureFlags: [],
});

const config = (state = initialConfigState, action: any) => {
  switch (action.type) {
    case FEATURE_FLAG:
      return state.set('featureFlags', fromJS(action.featureFlags));
    default:
      return state;
  }
};

const reducers = combineReducers<any>({
  login,
  listing,
  risk,
  operations,
  request,
  manage,
  details,
  form,
  home,
  config,
  templates,
});

export default reducers;
