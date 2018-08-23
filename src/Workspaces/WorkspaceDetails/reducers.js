import {
  GET_WORKSPACE,
  SET_WORKSPACE,
  SET_TAB,
} from './actions';

const initialState = {
  fetching: false,
  activeWorkspace: false,
  activeTab: 'Status',
};

const details = (state = initialState, action) => {
  switch (action.type) {
    case GET_WORKSPACE:
      return {
        ...state,
        fetching: true,
      };
    case SET_WORKSPACE:
      return {
        ...state,
        fetching: false,
        activeWorkspace: {
          ...action.workspace,
          approved: action.workspace.approvals && action.workspace.approvals.infra && action.workspace.approvals.risk
        },
      }
    case SET_TAB:
      return {
        ...state,
        activeTab: action.tab,
      };
    default:
      return state;
  }
}

export default details;
