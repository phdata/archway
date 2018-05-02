import {GOVERNED_DATASET_DETAILS} from "./actions";

const initialState = {
    dataset: false,
    members: false
};

function governedDatasetDetails(state = initialState, action) {
    switch(action.type) {
        case GOVERNED_DATASET_DETAILS:
            return {
                ...state,
                dataset: action.dataset
            };


        default:
            return state;
    }
}

export default governedDatasetDetails;