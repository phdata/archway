import {
    REQUEST_INFORMATION_AREAS,
    INFORMATION_AREAS_FAILED,
    INFORMATION_AREAS_REQUESTED,
    INFORMATION_AREAS_SUCCESS
} from './actions';

const initialState = {
    items: false,
    loading: true,
    error: false
};

function areas(state = initialState, action) {
    switch (action.type) {
        case INFORMATION_AREAS_REQUESTED:
            return {
                ...state,
                loading: true
            };
        case INFORMATION_AREAS_SUCCESS:
            return {
                ...state,
                loading: false,
                items: action.items,
                error: false
            };
        case INFORMATION_AREAS_FAILED:
            return {
                ...state,
                loading: false,
                items: false,
                error: action.error
            };
        case REQUEST_INFORMATION_AREAS:
            return {
                ...state,
                loading: true
            };
        default:
            return state
    }
}

export default areas;