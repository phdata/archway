import {
    SHARED_WORKSPACES_REQUESTED,
    SHARED_WORKSPACES_SUCCESS,
    SHARED_WORKSPACES_FAILED
} from '../actions';

import {LOCATION_CHANGE} from 'react-router-redux';

const initialState = {
    items: false,
    loading: true,
    error: false
};

function workspaces(state = initialState, action) {
    switch (action.type) {
        case SHARED_WORKSPACES_REQUESTED:
            return {
                ...state,
                loading: true
            };
        case SHARED_WORKSPACES_SUCCESS:
            return {
                ...state,
                loading: false,
                items: action.items,
                error: false
            };
        case SHARED_WORKSPACES_FAILED:
            return {
                ...state,
                loading: false,
                items: false,
                error: action.error
            };
        default:
            return state
    }
}

export default workspaces;