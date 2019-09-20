import { ComplianceContent, Question, LinksGroup, Link } from '../../models/Manage';

export const REQUEST_NEW_COMPLIANCE = 'REQUEST_NEW_COMPLIANCE';
export const requestNewCompliance = () => ({
  type: REQUEST_NEW_COMPLIANCE,
});

export const REQUEST_UPDATE_COMPLIANCE = 'REQUEST_UPDATE_COMPLIANCE';
export const requestUpdateCompliance = () => ({
  type: REQUEST_UPDATE_COMPLIANCE,
});

export const REQUEST_DELETE_COMPLIANCE = 'REQUEST_DELETE_COMPLIANCE';
export const requestDeleteCompliance = () => ({
  type: REQUEST_DELETE_COMPLIANCE,
});

export const SET_LOADING_STATUS = 'SET_LOADING_STATUS';
export const setLoadingStatus = (loading: boolean) => ({
  type: SET_LOADING_STATUS,
  loading,
});

export const GET_COMPLIANCES = 'GET_COMPLIANCES';
export const getCompliances = () => ({
  type: GET_COMPLIANCES,
});

export const SET_COMPLIANCES = 'SET_COMPLIANCES';
export const setCompliances = (compliances: ComplianceContent[]) => ({
  type: SET_COMPLIANCES,
  compliances,
});

export const SET_SELECTED_COMPLIANCE = 'SET_SELECTED_COMPLIANCE';
export const setSelectedCompliance = (compliance: ComplianceContent) => ({
  type: SET_SELECTED_COMPLIANCE,
  compliance,
});

export const CLEAR_SELECTED_COMPLIANCE = 'CLEAR_SELECTED_COMPLIANCE';
export const clearSelectedCompliance = () => ({
  type: CLEAR_SELECTED_COMPLIANCE,
});

export const SET_QUESTION = 'SET_QUESTION';
export const setQuestion = (index: number, question: Question) => ({
  type: SET_QUESTION,
  objQuestion: {
    index,
    question,
  },
});

export const ADD_NEW_QUESTION = 'ADD_NEW_QUESTION';
export const addNewQuestion = (question: Question) => ({
  type: ADD_NEW_QUESTION,
  question,
});

export const REMOVE_QUESTION = 'REMOVE_QUESTION';
export const removeQuestion = (index: number) => ({
  type: REMOVE_QUESTION,
  index,
});

export const GET_LINKSGROUPS = 'GET_LINKSGROUPS';
export const getLinksGroups = () => ({
  type: GET_LINKSGROUPS,
});

export const SET_LINKSGROUPS = 'SET_LINKSGROUPS';
export const setLinksGroups = (linksGroups: LinksGroup[]) => ({
  type: SET_LINKSGROUPS,
  linksGroups,
});

export const REQUEST_NEW_LINKSGROUP = 'REQUEST_NEW_LINKSGROUP';
export const requestNewLinksGroup = () => ({
  type: REQUEST_NEW_LINKSGROUP,
});

export const REQUEST_UPDATE_LINKSGROUP = 'REQUEST_UPDATE_LINKSGROUP';
export const requestUpdateLinksGroup = () => ({
  type: REQUEST_UPDATE_LINKSGROUP,
});

export const REQUEST_DELETE_LINKSGROUP = 'REQUEST_DELETE_LINKSGROUP';
export const requestDeleteLinksGroup = () => ({
  type: REQUEST_DELETE_LINKSGROUP,
});

export const CLEAR_SELECTED_LINKSGROUP = 'CLEAR_SELECTED_LINKSGROUP';
export const clearSelectedLinksGroup = () => ({
  type: CLEAR_SELECTED_LINKSGROUP,
});

export const SET_SELECTED_LINKSGROUP = 'SET_SELECTED_LINKSGROUP';
export const setSelectedLinksGroup = (linksGroup: LinksGroup) => ({
  type: SET_SELECTED_LINKSGROUP,
  linksGroup,
});

export const SET_LINK = 'SET_LINK';
export const setLink = (index: number, link: Link) => ({
  type: SET_LINK,
  objLink: {
    index,
    link,
  },
});

export const ADD_NEW_LINK = 'ADD_NEW_LINK';
export const addNewLink = (link: Link) => ({
  type: ADD_NEW_LINK,
  link,
});

export const REMOVE_LINK = 'REMOVE_LINK';
export const removeLink = (index: number) => ({
  type: REMOVE_LINK,
  index,
});
