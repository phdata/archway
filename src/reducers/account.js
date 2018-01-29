import { combineReducers } from 'redux'
import {
    LOGIN_REQUEST,
    LOGIN_SUCCESS,
    LOGIN_FAILURE
} from '../actions'

const initialState = {
    token: null,
    error: null,
    loggingIn: false
};

function token(state = initialState, action ) {
    switch (action.type) {
        case LOGIN_REQUEST:
            return {
                ...state,
                loggingIn: true
            };
        case LOGIN_SUCCESS:
            return {
                loggingIn: false,
                token: action.token
            };
        case LOGIN_FAILURE:
            return {
                loggingIn: false,
                error: action.error
            };
        default:
            return state
    }
}

export default combineReducers({
    token
})