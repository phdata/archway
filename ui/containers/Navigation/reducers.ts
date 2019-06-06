import { fromJS } from 'immutable';

import { CLUSTER_INFO } from './actions';

const initialState = fromJS({
  details: {
    name: 'Unknown',
    status: 'unknown',
    services: {
      hue: false,
      hive: false,
      yarn: false,
      mgmt: false
    }
  }
});

function cluster(state = initialState, action: any) {
  switch (action.type) {
    case CLUSTER_INFO:
      return state.set('details', fromJS(action.cluster[0]));
    default:
      return state;
  }
}

export default cluster;
