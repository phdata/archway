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
  SET_MEMBERS,
  NEW_MEMBER_FORM_CHANGED,
  CHANGE_DB,
  FILTER_CHANGED,
  SET_FILTERED_LIST,
} from './actions';

const initialState = {
  generating: false,
  fetching: false,
  workspaceList: [],
  filteredList: [],
  activeWorkspace: null,
  pendingRequestType: null,
  pendingRequest: { compliance: {} },
  pendingWorkspace: null,
  requesting: false,
  approving: false,
  members: {},
  newMemberForm: { role: 'manager' },
  searchForm: { filter: '' },
  memberForm: { filter: '' },
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
        activeWorkspace: {
          ...action.workspace,
          status: action.workspace.approvals && action.workspace.approvals.length == 2 ? 'Approved' : 'Pending'
        },
      };
    case APPROVE_WORKSPACE_COMPLETED:
      return {
        ...state,
        approving: false,
      };
    case SET_MEMBERS:
      return {
        ...state,
        existingMembers: action.members,
      };
    case NEW_MEMBER_FORM_CHANGED:
      const memberFieldKey = Object.keys(action.field)[0];
      state.newMemberForm[memberFieldKey] = action.field[memberFieldKey].value;
      return state;
    case CHANGE_DB:
      return {
        ...state,
        activeDatabase: action.name,
      };
    case FILTER_CHANGED:
      return {
        ...state,
        searchForm: { filter: action.filter }
      };
    case SET_FILTERED_LIST:
      return {
        ...state,
        filteredList: action.filteredList,
      };
    default:
      return state;
  }
};

export default workspaces;