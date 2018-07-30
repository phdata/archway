import {
  SET_MEMBERS,
  MEMBER_FILTER_CHANGED,
  EXISTING_MEMBER_SELECTED,
  SET_FILTERED_LIST,
} from './actions';

const initialState = {
  existingMembers: false,
  newMembers: false,
  filteredMembers: false,
  memberForm: { filter: '' },
  selectedUser: false,
}

const members = (state = initialState, action) => {
  switch (action.type) {
    case SET_MEMBERS:
      return {
        ...state,
        existingMembers: action.members,
      };
    case MEMBER_FILTER_CHANGED:
      return {
        ...state,
        memberForm: { filter: action.filter },
        newMembers: action.filter === '' ? false : [{ username: action.filter }]
      };
    case EXISTING_MEMBER_SELECTED:
      return {
        ...state,
        selectedUser: action.user,
      };
    case SET_FILTERED_LIST:
      return {
        ...state,
        filteredMembers: action.members,
      };
    default:
      return state;
  }
}

export default members;