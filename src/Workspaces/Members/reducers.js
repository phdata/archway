import {
  SET_MEMBERS,
  MEMBER_FILTER_CHANGED,
  EXISTING_MEMBER_SELECTED,
} from './actions';

const initialState = {
  existingMembers: false,
  newMembers: false,
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
        memberForm: { filter: action.filter }
      };
    case EXISTING_MEMBER_SELECTED:
      return {
        ...state,
        selectedUser: action.user,
      };
    default:
      return state;
  }
}

export default members;
