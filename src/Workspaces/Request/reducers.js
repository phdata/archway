import {
  SET_REQUEST,
  SET_REQUEST_TYPE,
  WORKSPACE_GENERATED,
  SET_GENERATING,
  REQUEST_CHANGED,
  WORKSPACE_REQUESTED,
} from './actions';

const initialState = {
  generating: false,
  pendingRequestType: null,
  pendingRequest: { compliance: {} },
  pendingWorkspace: null,
  requesting: false,
};

const request = (state = initialState, action) => {
  switch (action.type) {
    case SET_REQUEST:
      return {
        ...state,
        pendingRequest: action.request,
      };
    case SET_REQUEST_TYPE:
      return {
        ...state,
        pendingRequestType: action.requestType,
      };
    case WORKSPACE_GENERATED:
      return {
        ...state,
        pendingWorkspace: action.workspace,
      };
    case SET_GENERATING:
      return {
        ...state,
        generating: action.generating,
      };
    case REQUEST_CHANGED:
      const key = Object.keys(action.field)[0];
      if (key === 'compliance') {
        state.pendingRequest.compliance = {
          pci_data: (action.field.compliance.value.indexOf('pci') >= 0),
          phi_data: (action.field.compliance.value.indexOf('phi') >= 0),
          pii_data: (action.field.compliance.value.indexOf('pii') >= 0),
        };
      } else {
        state.pendingRequest[key] = action.field[key].value;
      }

      return state;
    case WORKSPACE_REQUESTED:
      return {
        ...state,
        requesting: true,
      };
    default:
      return state;
  }
};

export default request;
