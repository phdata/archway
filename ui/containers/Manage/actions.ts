import { ComplianceContent, Question } from '../../models/Manage';

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
