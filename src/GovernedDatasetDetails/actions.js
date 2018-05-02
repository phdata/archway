export const GOVERNED_DATASET_DETAILS = "GOVERNED_DATASET_DETAILS";

export function governedDatasetDetails(dataset) {
    return {
        type: GOVERNED_DATASET_DETAILS,
        dataset
    }
}