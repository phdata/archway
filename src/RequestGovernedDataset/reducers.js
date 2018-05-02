import {REQUEST_GOVERNED_DATASET} from "./actions";

const initialState = {
    requesting: false
};

const requestWorkspace = (state = initialState, action) => {
    switch(action.type) {
        case REQUEST_GOVERNED_DATASET:
            return {
                ...state,
                requesting: true
            };
        default:
            return state;
    }
};

export default requestWorkspace;