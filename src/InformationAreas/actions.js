export const REQUEST_INFORMATION_AREAS = "REQUEST_INFORMATION_AREAS";

export const INFORMATION_AREAS_REQUESTED = "INFORMATION_AREAS_REQUESTED";
export const INFORMATION_AREAS_SUCCESS = "INFORMATION_AREAS_SUCCESS";
export const INFORMATION_AREAS_FAILED = "INFORMATION_AREAS_FAILED";

export function areasRequested() {
    return {
        type: INFORMATION_AREAS_REQUESTED
    };
}

export function areasSuccess(areas) {
    return {
        type: INFORMATION_AREAS_SUCCESS,
        items: areas
    };
}

export function areasFailed(error) {
    return {
        type: INFORMATION_AREAS_FAILED,
        error
    };
}