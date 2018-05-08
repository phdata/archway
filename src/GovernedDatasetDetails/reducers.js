import {DATASET_MEMBER_LIST, GOVERNED_DATASET_DETAILS, GOVERNED_DATASET_SELECTED} from "./actions";

const initialState = {
    dataset: false,
    members: false,
    active: {}
};

function governedDatasetDetails(state = initialState, action) {
    switch(action.type) {
        case GOVERNED_DATASET_DETAILS:
            return {
                ...state,
                dataset: action.dataset,
                active: {
                    ...state.active,
                    dataset: action.dataset.raw,
                    name: "raw"
                }
            };
        case GOVERNED_DATASET_SELECTED:
            return {
                ...state,
                active: {
                    ...state.active,
                    dataset: state.dataset[action.name],
                    name: action.name,
                    members: false
                }
            };
        case DATASET_MEMBER_LIST:
            return {
                ...state,
                active: {
                    ...state.active,
                    members: action.members
                }
            };


        default:
            return state;
    }
}

export default governedDatasetDetails;