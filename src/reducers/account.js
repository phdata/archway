import { combineReducers } from 'redux'
import {
    LOGIN_REQUEST,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    TOKEN_EXTRACTED,
    TOKEN_NOT_AVAILABLE
} from '../actions'

const initialState = {
    token: null,
    error: null,
    loggingIn: false,
    loading: true
};

function token(state = initialState, action ) {
    console.log(action);
    switch (action.type) {
        case LOGIN_REQUEST:
            return {
                ...state,
                loggingIn: true
            };
        case LOGIN_SUCCESS:
            return {
                ...state,
                loggingIn: false,
                token: action.token
            };
        case LOGIN_FAILURE:
            return {
                ...state,
                loggingIn: false,
                error: action.error
            };
        case TOKEN_EXTRACTED:
            return {
                ...state,
                loading: false,
                token: action.token
            };
        case TOKEN_NOT_AVAILABLE:
            return {
                ...state,
                loading: false
            };
        default:
            return state
    }
}

export default combineReducers({
    account: token
});