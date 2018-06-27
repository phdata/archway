import {
  CHANGE_ACTIVE_WORKSPACE,
  WORKSPACE_GENERATED,
  WORKSPACE_REQUESTED,
  REQUEST_CHANGED,
  LIST_WORKSPACES,
  SET_REQUEST_TYPE,
  SET_REQUEST,
  SET_GENERATING,
  SET_WORKSPACE,
  SET_WORKSPACE_LIST,
  GET_WORKSPACE,
  APPROVE_WORKSPACE_REQUESTED,
  APPROVE_WORKSPACE_COMPLETED,
  SET_MANAGERS,
  SET_READONLY,
} from './actions';

const initialState = {
  generating: false,
  fetching: false,
  workspaceList: [],
  activeWorkspace: null,
  pendingRequestType: null,
  pendingRequest: { compliance: {} },
  pendingWorkspace: null,
  requesting: false,
  approving: false,
  members: {},
};

const workspaces = (state = initialState, action) => {
  switch (action.type) {
    case CHANGE_ACTIVE_WORKSPACE:
      return {
        ...state,
        activeWorkspace: action.workspace,
      };
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
    case SET_WORKSPACE_LIST:
      return {
        ...state,
        fetching: false,
        workspaceList: action.workspaceList,
      };
    case LIST_WORKSPACES:
      return {
        ...state,
        fetching: true,
      };
    case APPROVE_WORKSPACE_REQUESTED:
      return {
        ...state,
        approving: true,
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
    case GET_WORKSPACE:
      return {
        ...state,
        fetching: true,
      };
    case SET_WORKSPACE:
      return {
        ...state,
        activeWorkspace: action.workspace,
      };
    case APPROVE_WORKSPACE_COMPLETED:
      return {
        ...state,
        approving: false,
      };
    case SET_READONLY:
      return {
        ...state,
        members: {
          ...state.members,
          readonly: action.members,
        },
      };
    case SET_MANAGERS:
      return {
        ...state,
        members: {
          ...state.members,
          managers: action.members,
        },
      };
    default:
      return state;
  }
};

export default workspaces;
