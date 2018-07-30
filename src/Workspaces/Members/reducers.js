import {
  SET_MEMBERS,
  MEMBER_FILTER_CHANGED,
 } from './actions';

const initialState = {
  existingMembers: false,
  newMembers: false,
  memberForm: { filter: '' },
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
      }
    default:
      return state;
  }
}

export default members;
