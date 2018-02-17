export const REQUEST_SHARED_WORKSPACE = "REQUEST_SHARED_WORKSPACE";


export function requestSharedWorkspace({name, purpose, phi_data, pii_data, pci_data}) {
    return {
        type: REQUEST_SHARED_WORKSPACE,
        request: {
            name,
            purpose,
            compliance: {
                pii_data,
                pci_data,
                phi_data
            },
            hdfs: {
                requested_size_in_gb: 12
            },
            yarn: {
                max_cores: 10,
                max_memory_in_gb: 8
            }
        }
    }
}