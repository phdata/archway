export const GOVERNED_DATASET_DETAILS = "GOVERNED_DATASET_DETAILS";

export function governedDatasetDetails(dataset) {
    return {
        type: GOVERNED_DATASET_DETAILS,
        dataset
    }
}

export const GOVERNED_DATASET_SELECTED = "GOVERNED_DATASET_SELECTED";

export function setDataset(name) {
    return {
        type: GOVERNED_DATASET_SELECTED,
        name
    }
}

export const DATASET_MEMBER_LIST = "DATASET_MEMBER_LIST";

export function datasetMemberList(members) {
    return {
        type: DATASET_MEMBER_LIST,
        members
    }
}

export const DATASET_MEMBER_REQUESTED = "DATASET_MEMBER_REQUESTED";

export function requestNewMember({username}) {
    return {
        type: DATASET_MEMBER_REQUESTED,
        username
    }
}

export const DATASET_MEMBER_REMOVE = "DATASET_MEMBER_REMOVE";

export function removeMember(id, username) {
    return {
        type: DATASET_MEMBER_REMOVE,
        id,
        username
    }
}