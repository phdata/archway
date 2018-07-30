import {
  SET_WORKSPACE_LIST,
  LIST_WORKSPACES,
  FILTER_CHANGED,
  SET_FILTERED_LIST,
} from './actions';

const initialState = {
  fetching: false,
  workspaceList: [],
  filteredList: [],
  searchForm: { filter: '' },
};

const listing = (state = initialState, action) => {
  switch (action.type) {
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
}

export default listing;
