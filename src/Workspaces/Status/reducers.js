import {
  APPROVE_WORKSPACE_COMPLETED,
  APPROVE_WORKSPACE_REQUESTED,
} from './actions.js';

const initialState = {
  approving: false,
};

const status = (state = initialState, action) => {
  switch (action.type) {
    case APPROVE_WORKSPACE_REQUESTED:
      return {
        ...state,
        approving: true,
      };
    case APPROVE_WORKSPACE_COMPLETED:
      return {
        ...state,
        approving: false,
      };
    default:
      return state;
  }
}

export default status;
