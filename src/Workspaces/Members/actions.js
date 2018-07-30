export const SET_MEMBERS = 'SET_MEMBERS';

export function setMembers(members) {
  return {
    type: SET_MEMBERS,
    members,
  };
}

export const NEW_MEMBER_SELECTED = 'NEW_MEMBER_SELECTED';

export function newMemberSelected(username) {
  return {
    type: NEW_MEMBER_SELECTED,
    username,
  }
}

export const EXISTING_MEMBER_SELECTED = 'EXISTING_MEMBER_SELECTED';

export function existingMemberSelected(user) {
  return {
    type: EXISTING_MEMBER_SELECTED,
    user,
  }
}

export const MEMBER_FILTER_CHANGED = 'MEMBER_FILTER_CHANGED';

export function memberFilterChanged(field) {
  return {
    type: MEMBER_FILTER_CHANGED,
    filter: field.filter.value
  };
}

export const GET_MEMBERS = 'GET_MEMBERS';

export function getMembers() {
  return {
    type: GET_MEMBERS,
  };
}
