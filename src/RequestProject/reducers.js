import {REQUEST_SHARED_WORKSPACE} from "./actions";

const initialState = {
    requesting: false
};

const requestWorkspace = (state = initialState, action) => {
    switch(action.type) {
        case REQUEST_SHARED_WORKSPACE:
            return {
                ...state,
                requesting: true
            };
        default:
            return state;
    }
};

export default requestWorkspace;